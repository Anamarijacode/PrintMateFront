package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class RezervniDio {
    @SerializedName("idRezervnogDijela") public int id;
    @SerializedName("tipDijelova")        public TipDijelova tip;
    @SerializedName("kolicina")           public int kolicina;
    @SerializedName("stanje")             public String stanje;
}
