/* Proizvodjac.java */
package com.printmate.PrintMate.Modeli;

import android.graphics.Bitmap;
import com.google.gson.annotations.SerializedName;

public class Proizvodjac {
    @SerializedName("idProizvodjaca")
    private int id;

    @SerializedName("naziv")
    private String naziv;

    @SerializedName("logo")
    private String logoBase64;

    // Prijeko potrebno: privremeno polje za dekodirani logo
    private Bitmap logoBitmap;

    public int getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public Bitmap getLogoBitmap() {
        return logoBitmap;
    }

    public void setLogoBitmap(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;
    }
}
