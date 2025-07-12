package com.biletapp.biletapp.repository;

import com.biletapp.biletapp.model.Etkinlik;
import org.springframework.data.jpa.repository.Query; // BU İMPORT GEREKLİ!
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EtkinlikRepository extends JpaRepository<Etkinlik, Long> {
    List<Etkinlik> findByKonum(String konum);

    @Query("SELECT DISTINCT e.konum FROM Etkinlik e")
    List<String> findDistinctKonumlar();

    List<Etkinlik> findByTur(Etkinlik.EtkinlikTuru tur);

}