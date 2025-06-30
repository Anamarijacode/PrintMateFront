package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class BojaMaterijala {
    @SerializedName("idBojeMaterijala") public int id;
    @SerializedName("naziv")             public String naziv;
    @SerializedName("hexKod")            public String hexKod;
}