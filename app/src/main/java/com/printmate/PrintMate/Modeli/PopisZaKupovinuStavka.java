package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class PopisZaKupovinuStavka {
    @SerializedName("idStavke")         public int id;
    @SerializedName("naziv")            public String naziv;
    @SerializedName("kolicina")         public double kolicina;
    @SerializedName("tipMaterijala")    public TipMaterijala tip;   // nullable
    @SerializedName("bojaMaterijala")   public BojaMaterijala boja; // nullable
}