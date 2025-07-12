package com.biletapp.biletapp.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biletapp.biletapp.model.User;
import com.biletapp.biletapp.repository.BiletRepository;
import com.biletapp.biletapp.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/kullanici")
public class UserController {
    private final UserRepository userRepository;
    private final BiletRepository biletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, BiletRepository biletRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.biletRepository = biletRepository;
    }

    @GetMapping("/profil")
    public String profilSayfasi(Principal principal, Model model) {
        User kullanici = userRepository.findByUsername(principal.getName()).orElseThrow();
        int biletSayisi = biletRepository.findByKullanici(kullanici).size();

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("biletSayisi", biletSayisi);
        return "/profil";
    }
    @PostMapping("/profil/guncelle")
    public String profilGuncelle(Principal principal, @RequestParam String email) {
        User kullanici = userRepository.findByUsername(principal.getName()).orElseThrow();
        kullanici.setEmail(email);
        userRepository.save(kullanici);
        return "redirect:/kullanici/profil";
    }
    @PostMapping("/profil/sifre-guncelle")
    public String sifreGuncelle(Principal principal,
                                @RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                Model model) {

        User kullanici = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, kullanici.getPassword())) {
            model.addAttribute("sifreHatasi", "Mevcut şifre yanlış!");
            model.addAttribute("kullanici", kullanici);
            model.addAttribute("biletSayisi", biletRepository.findByKullanici(kullanici).size());
            return "/kullanici/profil";
        }

        kullanici.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(kullanici);
        return "redirect:/kullanici/profil";
    }
}
