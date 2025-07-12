package com.biletapp.biletapp.repository;

import com.biletapp.biletapp.model.Koltuk;
import com.biletapp.biletapp.model.Etkinlik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KoltukRepository extends JpaRepository<Koltuk, Long> {
    
    List<Koltuk> findByEtkinlik(Etkinlik etkinlik);

    Optional<Koltuk> findByEtkinlikAndSiraNoAndKoltukNo(Etkinlik etkinlik, int siraNo, int koltukNo);

    List<Koltuk> findByEtkinlikId(Long etkinlikId);

    List<Koltuk> findByEtkinlikOrderBySiraNoAscKoltukNoAsc(Etkinlik etkinlik);

}
