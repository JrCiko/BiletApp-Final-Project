package com.biletapp.biletapp.controller;

import com.biletapp.biletapp.model.Bilet;
import com.biletapp.biletapp.model.BiletTipi;
import com.biletapp.biletapp.model.Etkinlik;
import com.biletapp.biletapp.model.Etkinlik.EtkinlikTuru;
import com.biletapp.biletapp.model.Koltuk;
import com.biletapp.biletapp.model.RefundRequest;
import com.biletapp.biletapp.model.RefundRequest.RefundStatus;
import com.biletapp.biletapp.model.User;
import com.biletapp.biletapp.repository.EtkinlikRepository;
import com.biletapp.biletapp.repository.BiletRepository;
import com.biletapp.biletapp.repository.KoltukRepository;
import com.biletapp.biletapp.repository.UserRepository;
import com.biletapp.biletapp.repository.RefundRequestRepository;
import com.biletapp.biletapp.dto.RefundRequestDTO;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final EtkinlikRepository etkinlikRepository;
    private final BiletRepository biletRepository;
    private final KoltukRepository koltukRepository;
    private final UserRepository kullaniciRepository;
    private final RefundRequestRepository refundRequestRepository;

    // Constructor Injection (Spring otomatik bağlar)
    public HomeController(EtkinlikRepository etkinlikRepository,
                          BiletRepository biletRepository,
                          KoltukRepository koltukRepository, UserRepository kullaniciRepository, RefundRequestRepository refundRequestRepository) {
        this.refundRequestRepository = refundRequestRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.etkinlikRepository = etkinlikRepository;
        this.biletRepository = biletRepository;
        this.koltukRepository = koltukRepository;
    }
//#region AnaSayfa
    @GetMapping("/")
    public String home(@RequestParam(required = false) String konum, Model model) {
        List<Etkinlik> etkinlikler;

        if (konum != null && !konum.isEmpty()) {
            etkinlikler = etkinlikRepository.findByKonum(konum);
        } else {
            etkinlikler = etkinlikRepository.findAll();
        }

        List<String> konumlar = etkinlikRepository.findDistinctKonumlar();

        model.addAttribute("etkinlikler", etkinlikler);
        model.addAttribute("konumlar", konumlar);
        model.addAttribute("secilenKonum", konum);

        return "index";
    }

    @GetMapping("/konum/{konum}")
    public String etkinlikleriKonumaGore(@PathVariable String konum, Model model) {
        List<Etkinlik> etkinlikler = etkinlikRepository.findByKonum(konum);
        model.addAttribute("etkinlikler", etkinlikler);
        return "etkinlikler"; // etkinlikler.html sayfası
    }

    @GetMapping("/admin/panel")
    public String adminPanel(@AuthenticationPrincipal User user, Model model) {
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("etkinlikler", etkinlikRepository.findAll());
        return "admin-panel";
    }
//#endregion
//region Bilet Alma
    @GetMapping("/bilet-al/{etkinlikId}")
    public String biletAlSayfasi(@PathVariable Long etkinlikId, Model model) {
        Optional<Etkinlik> optionalEtkinlik = etkinlikRepository.findById(etkinlikId);
        if (optionalEtkinlik.isEmpty()) {
            model.addAttribute("hataMesaji", "Etkinlik bulunamadı!");
            return "hataSayfasi";
        }

        Etkinlik etkinlik = optionalEtkinlik.get();
        model.addAttribute("etkinlik", etkinlik);

        if (etkinlik.getTur() == EtkinlikTuru.TIYATRO || etkinlik.getTur() == EtkinlikTuru.SPOR) {
            List<Koltuk> koltuklar = koltukRepository.findByEtkinlikOrderBySiraNoAscKoltukNoAsc(etkinlik);
            model.addAttribute("koltuklar", koltuklar);
        } else {
            model.addAttribute("koltuklar", Collections.emptyList());
        }

        return "bilet-al";
    }

    @PostMapping("/bilet-al")
    public String biletSatinAl(
            @RequestParam Long etkinlikId,
            @RequestParam(required = false) List<Long> koltukIds,
            @RequestParam BiletTipi biletTipi,
            Model model, Principal principal) {

        Optional<Etkinlik> optionalEtkinlik = etkinlikRepository.findById(etkinlikId);
        if (optionalEtkinlik.isEmpty()) {
            model.addAttribute("hataMesaji", "Etkinlik bulunamadı.");
            return "hataSayfasi";
        }

        Etkinlik etkinlik = optionalEtkinlik.get();
        User kullanici = kullaniciRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Bilet bilet = new Bilet();
        bilet.setKullanici(kullanici);
        bilet.setEtkinlik(etkinlik);
        double fiyat;

        switch (biletTipi) {
            case OGRENCI:
                fiyat = 50.0;
                break;
            case VIP:
                fiyat = 200.0;
                break;
            default:
                fiyat = 100.0;
                break;
        }

        bilet.setBiletTipi(biletTipi);
        bilet.setFiyat(fiyat);

        bilet.setSatinAlmaTarihi(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        List<Koltuk> secilenKoltuklar = new ArrayList<>();

        // Sadece koltuk gerektiren etkinlik türlerinde koltuk seçimi zorunlu
        if (etkinlik.getTur() == EtkinlikTuru.TIYATRO || etkinlik.getTur() == EtkinlikTuru.SPOR) {
            if (koltukIds == null || koltukIds.isEmpty()) {
                model.addAttribute("hataMesaji", "Lütfen en az bir koltuk seçiniz.");
                return "hataSayfasi";
            }

            // Koltuklar güncellenip bilet ile ilişkilendiriliyor
            for (Long id : koltukIds) {
                Optional<Koltuk> koltukOpt = koltukRepository.findById(id);
                koltukOpt.ifPresent(k -> {
                    k.setRezerve(true);
                    k.setBilet(bilet);
                    secilenKoltuklar.add(k);
                });
            }

            if (secilenKoltuklar.isEmpty()) {
                model.addAttribute("hataMesaji", "Seçilen koltuklar geçersiz.");
                return "hataSayfasi";
            }
        }

        // Koltuklar varsa set et, yoksa boş liste ata
        bilet.setKoltuklar(secilenKoltuklar);

        biletRepository.save(bilet);
        
        for (Koltuk k : secilenKoltuklar) {
            koltukRepository.save(k);  // ✅ garanti
        }
        model.addAttribute("bilet", bilet);
        model.addAttribute("secilenKoltuklar", secilenKoltuklar);
        return "biletiniz";
    }

//#endregion
//#region Biletlerim
    @GetMapping("/benim-biletlerim")
    public String benimBiletlerim(@RequestParam(required = false) String iptalBasarili, Model model, Principal principal) {
        String username = principal.getName();
        Optional<User> kullaniciOpt = kullaniciRepository.findByUsername(username);
        if (kullaniciOpt.isEmpty()) {
            model.addAttribute("hataMesaji", "Kullanıcı bulunamadı.");
            return "hataSayfasi";
        }

        User kullanici = kullaniciOpt.get();
        List<Bilet> biletler = biletRepository.findByKullanici(kullanici);

        model.addAttribute("biletler", biletler);
        if ("true".equals(iptalBasarili)) {
        model.addAttribute("mesaj", "Bilet başarıyla iptal edildi.");
    }
        return "benim-biletlerim";
    }
    @GetMapping("/bilet-detaylari/{biletId}")
    public String biletDetay(@PathVariable Long biletId, Model model, Principal principal) {
        Optional<Bilet> optionalBilet = biletRepository.findById(biletId);
        if (optionalBilet.isEmpty()) {
            model.addAttribute("hataMesaji", "Bilet bulunamadı!");
            return "hataSayfasi";
        }

        Bilet bilet = optionalBilet.get();

        // Güvenlik için bilet sahibi kontrolü
        if (!bilet.getKullanici().getUsername().equals(principal.getName())) {
            model.addAttribute("hataMesaji", "Bu bilete erişim yetkiniz yok!");
            return "hataSayfasi";
        }

        model.addAttribute("bilet", bilet);
        model.addAttribute("etkinlik", bilet.getEtkinlik());
        model.addAttribute("koltuklar", bilet.getKoltuklar());

        return "bilet-detaylari";
    }
    @PostMapping("/bilet-iptal/{biletId}")
    public String biletIptal(@PathVariable Long biletId, Principal principal, Model model) {
        Optional<Bilet> optionalBilet = biletRepository.findById(biletId);
        if (optionalBilet.isEmpty()) {
            model.addAttribute("hataMesaji", "Bilet bulunamadı!");
            return "hataSayfasi";
        }

        Bilet bilet = optionalBilet.get();

        if (!bilet.getKullanici().getUsername().equals(principal.getName())) {
            model.addAttribute("hataMesaji", "Bu bileti iptal etme yetkiniz yok!");
            return "hataSayfasi";
        }

        // Koltukların bilet alanını null yap
        if (bilet.getKoltuklar() != null) {
            for (Koltuk koltuk : bilet.getKoltuklar()) {
                koltuk.setBilet(null);
                koltuk.setRezerve(false);
                koltukRepository.save(koltuk);
            }
        }

        // Biletin koltuk listesini temizle (optional ama önerilir)
        bilet.getKoltuklar().clear();
        biletRepository.delete(bilet);

        return "redirect:/benim-biletlerim?iptalBasarili=true";
    }
//#endregion
    @PostMapping("/refund-request")
    public String createRefundRequest(@ModelAttribute RefundRequestDTO dto, Principal principal, Model model) {
        Optional<Bilet> optionalBilet = biletRepository.findById(dto.getBiletId());

        if (optionalBilet.isEmpty()) {
            model.addAttribute("error", "Bilet bulunamadı.");
            return "error"; // veya redirect:/profile gibi bir sayfa
        }

        User user = kullaniciRepository.findByUsername(principal.getName())
    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));


        RefundRequest request = new RefundRequest();
        request.setBilet(optionalBilet.get());
        request.setUser(user);
        request.setReason(dto.getReason());
        request.setStatus(RefundStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        refundRequestRepository.save(request);

        model.addAttribute("message", "İade talebiniz alınmıştır.");
        return "redirect:/profile"; // biletlerim sayfasına yönlendirme gibi
    }
}
