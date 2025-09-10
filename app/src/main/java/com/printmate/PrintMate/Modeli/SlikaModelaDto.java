package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class SlikaModelaDto {
    @SerializedName("idSlike")
    private int idSlike;
    @SerializedName("gKodId")
    private int gKodId;
    @SerializedName("datum")
    private String datum;

    public int getIdSlike() { return idSlike; }
    public int getGKodId()   { return gKodId;   }
    public String getDatum() { return datum;   }
}
