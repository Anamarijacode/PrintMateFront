// com/printmate/PrintMate/Modeli/Printer.java
package com.printmate.PrintMate.Modeli;

import android.graphics.Bitmap;
import com.google.gson.annotations.SerializedName;

public class Printer {
    @SerializedName("idModela")
    private int idModela;

    @SerializedName("naziv")
    private String naziv;

    // Backend vraća sliku kao Base64 string
    @SerializedName("slikaPrintera")
    private String slikaPrinteraBase64;

    // Ovo polje mora imati istu JSON ključ-naziv koji backend šalje:
    @SerializedName("proizvodjacPrinteraId")
    private int proizvodjacId;

    // privremeno polje za dekodiranu Bitmapu
    private Bitmap logoBitmap;

    // ————— getters —————
    public int getIdModela() {
        return idModela;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getSlikaPrinteraBase64() {
        return slikaPrinteraBase64;
    }

    /** ID proizvođača (koristi za filtriranje) */
    public int getProizvodjacId() {
        return proizvodjacId;
    }

    public Bitmap getLogoBitmap() {
        return logoBitmap;
    }

    // ————— setters —————
    public void setLogoBitmap(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;
    }
}
