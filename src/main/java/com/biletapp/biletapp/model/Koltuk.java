package com.biletapp.biletapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Koltuk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int siraNo;
    private int koltukNo;

    private boolean rezerve;

    @ManyToOne
    @JoinColumn(name = "etkinlik_id")
    private Etkinlik etkinlik;

    @ManyToOne
    @JoinColumn(name = "bilet_id")  // bilet_id sütunu bu satırla tanımlanır
    private Bilet bilet;

    // Getter ve Setter'lar

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public int getSiraNo() {
        return siraNo;
    }
    public void setSiraNo(int siraNo) {
        this.siraNo = siraNo;
    }

    public int getKoltukNo() {
        return koltukNo;
    }
    public void setKoltukNo(int koltukNo) {
        this.koltukNo = koltukNo;
    }

    public boolean isRezerve() {
        return rezerve;
    }
    public void setRezerve(boolean rezerve) {
        this.rezerve = rezerve;
    }

    public Etkinlik getEtkinlik() {
        return etkinlik;
    }
    public void setEtkinlik(Etkinlik etkinlik) {
        this.etkinlik = etkinlik;
    }
    public Bilet getBilet() {
        return bilet;
    }
    public void setBilet(Bilet bilet) {
        this.bilet = bilet;
    }
}
