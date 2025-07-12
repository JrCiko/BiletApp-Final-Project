package com.biletapp.biletapp.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "biletler")
public class Bilet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kullanici_id")
    private User kullanici;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etkinlik_id")
    private Etkinlik etkinlik;

    @OneToMany(mappedBy = "bilet", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false, fetch = FetchType.EAGER)
    private List<Koltuk> koltuklar;

    @Enumerated(EnumType.STRING)
    private BiletTipi biletTipi;



    @Column(nullable = false)
    private Double fiyat;

    @Column(name = "satin_alma_tarihi", nullable = false)
    private String satinAlmaTarihi;

    // Getter & Setter’lar
    public List<Koltuk> getKoltuklar() {
        return koltuklar;
    }

    public void setKoltuklar(List<Koltuk> koltuklar) {
        this.koltuklar = koltuklar;
    }
    public BiletTipi getBiletTipi() {
        return biletTipi;
    }

    public void setBiletTipi(BiletTipi biletTipi) {
        this.biletTipi = biletTipi;
    }
}