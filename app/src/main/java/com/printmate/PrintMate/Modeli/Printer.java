package com.printmate.PrintMate.Model;

public class Printer {
    private String naziv;
    private int slikaResId;
    private String proizvodjacNaziv;

    public Printer(String naziv, int slikaResId, String proizvodjacNaziv) {
        this.naziv = naziv;
        this.slikaResId = slikaResId;
        this.proizvodjacNaziv = proizvodjacNaziv;
    }

    public String getNaziv() {
        return naziv;
    }

    public int getSlikaResId() {
        return slikaResId;
    }

    public String getProizvodjacNaziv() {
        return proizvodjacNaziv;
    }
}
