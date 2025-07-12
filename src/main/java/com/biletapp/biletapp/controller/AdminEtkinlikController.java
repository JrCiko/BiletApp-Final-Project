package com.biletapp.biletapp.controller;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.biletapp.biletapp.model.Bilet;
import com.biletapp.biletapp.model.Etkinlik;
import com.biletapp.biletapp.model.Koltuk;
import com.biletapp.biletapp.model.User;
import com.biletapp.biletapp.repository.BiletRepository;
import com.biletapp.biletapp.repository.EtkinlikRepository;
import com.biletapp.biletapp.repository.KoltukRepository;
import com.biletapp.biletapp.repository.RefundRequestRepository;

@Controller
@RequestMapping("/admin")
public class AdminEtkinlikController {

    private final EtkinlikRepository etkinlikRepository;
    private final KoltukRepository koltukRepository;
    private final BiletRepository biletRepository;
    private final RefundRequestRepository refundRequestRepository;

    public AdminEtkinlikController(EtkinlikRepository etkinlikRepository, KoltukRepository koltukRepository, BiletRepository biletRepository, RefundRequestRepository refundRequestRepository) {
        this.refundRequestRepository = refundRequestRepository;
        this.biletRepository = biletRepository;
        this.koltukRepository = koltukRepository;
        this.etkinlikRepository = etkinlikRepository;
    }
    @GetMapping
    public String adminHome(@AuthenticationPrincipal User user, Model model) {
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/";
        }
        List<Etkinlik> etkinlikler;
        etkinlikler = etkinlikRepository.findAll();
        model.addAttribute("admin", user);
        model.addAttribute("etkinlikler", etkinlikler);

        return "admin/index"; // src/main/resources/templates/admin/index.html
    }
    // Read - Listeleme
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("etkinlikler", etkinlikRepository.findAll());
        return "admin/etkinlik-list";  // views/admin/etkinlik-list.html
    }

    @GetMapping("/etkinlik/yeni")
    public String yeniEtkinlikFormu(Model model) {
        model.addAttribute("etkinlik", new Etkinlik());
        return "admin/yeni-etkinlik";
    }

    @PostMapping("/etkinlik/ekle")
    public String etkinlikKaydet(@ModelAttribute Etkinlik etkinlik) {
        // Etkinliği veritabanına kaydet
        Etkinlik kaydedilenEtkinlik = etkinlikRepository.save(etkinlik);

        // Eğer tür SPOR veya TIYATRO ise 50 koltuk ekle
        if (etkinlik.getTur() == Etkinlik.EtkinlikTuru.SPOR || etkinlik.getTur() == Etkinlik.EtkinlikTuru.TIYATRO) {
            for (int i = 0; i < 50; i++) {
                Koltuk koltuk = new Koltuk();
                koltuk.setSiraNo(i / 10 + 1);         // Her 10 koltuk yeni bir sıra
                koltuk.setKoltukNo(i % 10 + 1);       // 1'den 10'a kadar koltuk numarası
                koltuk.setRezerve(false);
                koltuk.setEtkinlik(kaydedilenEtkinlik);
                koltukRepository.save(koltuk);
            }
        }

        return "redirect:/admin";
    }
    // Update - Form gösterme
    @GetMapping("/duzenle/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Etkinlik etkinlik = etkinlikRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Etkinlik bulunamadı"));
        model.addAttribute("etkinlik", etkinlik);
        return "admin/etkinlik-form";
    }

    // Update - Form gönderme
    @PostMapping("/duzenle/{id}")
    public String editSubmit(@PathVariable Long id, @ModelAttribute Etkinlik etkinlik) {
        etkinlik.setId(id);
        etkinlikRepository.save(etkinlik);
        return "redirect:/admin";
    }

    // Delete
    @GetMapping("/sil/{id}")
    public String delete(@PathVariable Long id) {
        etkinlikRepository.deleteById(id);
        return "redirect:/admin";
    }
    @GetMapping("/etkinlik/{id}/katilimcilar")
    public String etkinlikKatilimcilari(@PathVariable Long id, Model model) {
    Optional<Etkinlik> optionalEtkinlik = etkinlikRepository.findById(id);
    if (optionalEtkinlik.isEmpty()) {
        model.addAttribute("hataMesaji", "Etkinlik bulunamadı!");
        return "hataSayfasi";
    }

    Etkinlik etkinlik = optionalEtkinlik.get();
    List<Bilet> biletler = biletRepository.findByEtkinlik(etkinlik);

    model.addAttribute("etkinlik", etkinlik);
    model.addAttribute("biletler", biletler);
    return "admin/katilimcilar";
    }
    @GetMapping("/raporlar")
    public String raporlar(Model model) {
    // Bugünün başlangıç ve bitiş saatini al
    LocalDateTime now = LocalDateTime.now();
    String todayStart = now.toLocalDate().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String todayEnd = now.toLocalDate().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    int bugunSatilanBiletSayisi = biletRepository.countBySatinAlmaTarihiBetween(todayStart, todayEnd);
    model.addAttribute("bugunSatilan", bugunSatilanBiletSayisi);

    // Etkinliklere göre satış raporu
    List<Etkinlik> etkinlikler = etkinlikRepository.findAll();

    List<Map<String, Object>> etkinlikRaporlari = new ArrayList<>();

    for (Etkinlik etkinlik : etkinlikler) {
        int toplamKoltuk = koltukRepository.findByEtkinlik(etkinlik).size();
        int rezerveKoltukSayisi = (int) koltukRepository.findByEtkinlik(etkinlik).stream()
        .filter(Koltuk::isRezerve)
        .count();

        double doluluk = toplamKoltuk > 0 ? (rezerveKoltukSayisi * 100.0 / toplamKoltuk) : 0;


        Map<String, Object> rapor = new HashMap<>();
        rapor.put("etkinlikAd", etkinlik.getAd());
        rapor.put("toplamKoltuk", toplamKoltuk);
        rapor.put("rezerveKoltukSayisi", rezerveKoltukSayisi);
        rapor.put("dolulukOrani", String.format("%.1f", doluluk)); // yüzde
        etkinlikRaporlari.add(rapor);
    }

    // Haftalık iptal istatistikleri
    LocalDateTime thisWeekStart = now.minusDays(7);
    LocalDateTime lastWeekStart = now.minusDays(14);
    LocalDateTime lastWeekEnd = now.minusDays(7);

    int thisWeekRefunds = refundRequestRepository.countByRequestDateBetween(thisWeekStart, now);
    int lastWeekRefunds = refundRequestRepository.countByRequestDateBetween(lastWeekStart, lastWeekEnd);

    int fark = thisWeekRefunds - lastWeekRefunds;

    model.addAttribute("thisWeekRefunds", thisWeekRefunds);
    model.addAttribute("lastWeekRefunds", lastWeekRefunds);
    model.addAttribute("fark", fark);


    model.addAttribute("etkinlikRaporlari", etkinlikRaporlari);

    return "admin/raporlar";
    }
}
