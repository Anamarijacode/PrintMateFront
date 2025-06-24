package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class ProizvodjacResponse {
    @SerializedName("idProizvodjaca")
    private int id;

    @SerializedName("naziv")
    private String naziv;

    // Ako server vraća logo kao Base64:
    @SerializedName("logo")
    private String logoBase64;

    // ili, ako vraća URL:
    // @SerializedName("logoUrl")
    // private String logoUrl;

    // getters
    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getLogoBase64() { return logoBase64; }
    // public String getLogoUrl() { return logoUrl; }
}
