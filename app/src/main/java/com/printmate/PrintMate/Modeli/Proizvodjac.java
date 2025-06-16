package com.printmate.PrintMate.Modeli;

public class Proizvodjac {
    private String naziv;
    private int slikaResId;

    public Proizvodjac(String naziv, int slikaResId) {
        this.naziv = naziv;
        this.slikaResId = slikaResId;
    }

    public String getNaziv() {
        return naziv;
    }

    public int getSlikaResId() {
        return slikaResId;
    }
}
