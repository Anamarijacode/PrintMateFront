// PrinterHome.java
package com.printmate.PrintMate.Modeli;

import android.graphics.Bitmap;
import com.google.gson.annotations.SerializedName;

public class PrinterHome {
    @SerializedName("idPrintera")
    private int idPrintera;

    @SerializedName("naziv")
    private String naziv;

    @SerializedName("modelId")
    private int modelPrintera;

    @SerializedName("ipAdresa")
    private String baseUrl;

    @SerializedName("apiKljucevi")
    private String apiKey;

    @SerializedName("online")
    private boolean online;

    @SerializedName("userId")
    private String userId;

    // Bitmap dekodirana iz Base64 (opcionalno)
    private Bitmap logoBitmap;

    // Status printera (npr. "Online"/"Offline")
    private String status;

    // Getters & Setters
    public int getIdPrintera() { return idPrintera; }
    public void setIdPrintera(int idPrintera) { this.idPrintera = idPrintera; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public int getModelPrintera() { return modelPrintera; }
    public void setModelPrintera(int modelPrintera) { this.modelPrintera = modelPrintera; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Bitmap getLogoBitmap() { return logoBitmap; }
    public void setLogoBitmap(Bitmap logoBitmap) { this.logoBitmap = logoBitmap; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
