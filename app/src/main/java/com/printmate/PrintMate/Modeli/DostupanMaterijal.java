package com.printmate.PrintMate.Modeli;


import com.google.gson.annotations.SerializedName;

public class DostupanMaterijal {
        @SerializedName("idMaterijla")    public int id;
        @SerializedName("tipMaterijala")  public TipMaterijala tip;
        @SerializedName("bojaMaterijala") public BojaMaterijala boja;
        @SerializedName("kolicina")       public double kolicina;
    }

