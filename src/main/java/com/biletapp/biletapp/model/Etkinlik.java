package com.biletapp.biletapp.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "etkinlikler")
public class Etkinlik {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String ad;

    @Column(name = "etkinlik_tarihi", nullable = false)
    private String tarih;

    @Column(name = "etkinlik_konumu", nullable = false)
    private String konum;

    @Enumerated(EnumType.STRING)
    @Column(name = "etkinlik_turu", nullable = false)
    private EtkinlikTuru tur;
    
    @OneToMany(mappedBy = "etkinlik", cascade = CascadeType.ALL)
    private List<Koltuk> koltuklar;

    public enum EtkinlikTuru {
        TIYATRO("Tiyatro"),
        SINEMA("Sinema"),
        MUZIK("Müzik"),
        SPOR("Spor"),
        DIGER("Diğer");

        private final String label;
        
        EtkinlikTuru(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static EtkinlikTuru fromLabel(String label) {
            for (EtkinlikTuru t : values()) {
                if (t.label.equalsIgnoreCase(label)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unknown label: " + label);
        }
    }


    public Etkinlik() {}

    public Etkinlik(Long id, String ad, String tarih, String konum, EtkinlikTuru tur) {
        this.id = id;
        this.ad = ad;
        this.tarih = tarih;
        this.konum = konum;
        this.tur = tur;
    }
}
