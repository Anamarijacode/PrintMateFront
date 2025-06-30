// com/printmate/PrintMate/Modeli/Gkod.java
package com.printmate.PrintMate.Modeli;

import com.google.gson.annotations.SerializedName;

public class Gkod {
    @SerializedName("idGKoda")
    private int id;

    @SerializedName("idUsera")
    private String userId;

    @SerializedName("naziv")
    private String naziv;

    @SerializedName("datumDodavanja")
    private String datumDodavanja;

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getDatumDodavanja() { return datumDodavanja; }
    public String getUserId() { return userId; }    // <— getter

    public void setId(int id) { this.id = id; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public void setDatumDodavanja(String datumDodavanja) { this.datumDodavanja = datumDodavanja; }
    public void setUserId(String userId) { this.userId = userId; }  // <— setter
}
