package com.biletapp.biletapp.repository;

import com.biletapp.biletapp.model.Bilet;
import com.biletapp.biletapp.model.Etkinlik;
import com.biletapp.biletapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BiletRepository extends JpaRepository<Bilet, Long> {
    List<Bilet> findByKullanici(User kullanici);

    List<Bilet> findByEtkinlik_Id(Long etkinlikId);

    List<Bilet> findByEtkinlik(Etkinlik etkinlik);

    int countBySatinAlmaTarihiBetween(String start, String end);

    @Query("SELECT b FROM Bilet b JOIN FETCH b.kullanici JOIN FETCH b.koltuklar WHERE b.etkinlik.id = :etkinlikId")
    List<Bilet> findByEtkinlikIdWithKoltuklar(@Param("etkinlikId") Long etkinlikId);



}
// BiletRepository, Bilet modeline ait CRUD işlemlerini yapar.
// Kullanıcıya ait biletleri listelemek için findByKullanici(User kullanici) metodunu içerir.