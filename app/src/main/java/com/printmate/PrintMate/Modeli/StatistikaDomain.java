package com.printmate.PrintMate.Modeli;

import java.time.Duration;

public class StatistikaDomain {
    private int id;
    private double ukupnaPotrosnjaMaterijala;
    private Duration ukupnoVrijemeRada;
    private int maximalnaTemperatura;
    private int minimalnaTemperatura;
    private int prosjecnaTemperatura;
    private int maximalnaBrzinaIspisa;
    private int minimalnaBrzinaIspisa;
    private int prosjecnaBrzinaIspisa;
    private String maximalnaBrzinaHladjenja;
    private String minimalnaBrzinaHladjenja;
    private String prosjecnaBrzinaHladjenja;
    private int gKodId;

    // — GENERATED GETTERS & SETTERS —
    public double getUkupnaPotrosnjaMaterijala() {
        return ukupnaPotrosnjaMaterijala;
    }
    public void setUkupnaPotrosnjaMaterijala(double v) {
        this.ukupnaPotrosnjaMaterijala = v;
    }

    public Duration getUkupnoVrijemeRada() {
        return ukupnoVrijemeRada;
    }
    public void setUkupnoVrijemeRada(Duration d) {
        this.ukupnoVrijemeRada = d;
    }

    public int getMaximalnaTemperatura() {
        return maximalnaTemperatura;
    }
    public void setMaximalnaTemperatura(int v) {
        this.maximalnaTemperatura = v;
    }

    public int getMinimalnaTemperatura() {
        return minimalnaTemperatura;
    }
    public void setMinimalnaTemperatura(int v) {
        this.minimalnaTemperatura = v;
    }

    public int getProsjecnaTemperatura() {
        return prosjecnaTemperatura;
    }
    public void setProsjecnaTemperatura(int v) {
        this.prosjecnaTemperatura = v;
    }

    public int getMaximalnaBrzinaIspisa() {
        return maximalnaBrzinaIspisa;
    }
    public void setMaximalnaBrzinaIspisa(int v) {
        this.maximalnaBrzinaIspisa = v;
    }

    public int getMinimalnaBrzinaIspisa() {
        return minimalnaBrzinaIspisa;
    }
    public void setMinimalnaBrzinaIspisa(int v) {
        this.minimalnaBrzinaIspisa = v;
    }

    public int getProsjecnaBrzinaIspisa() {
        return prosjecnaBrzinaIspisa;
    }
    public void setProsjecnaBrzinaIspisa(int v) {
        this.prosjecnaBrzinaIspisa = v;
    }

    public String getMaximalnaBrzinaHladjenja() {
        return maximalnaBrzinaHladjenja;
    }
    public void setMaximalnaBrzinaHladjenja(String v) {
        this.maximalnaBrzinaHladjenja = v;
    }

    public String getMinimalnaBrzinaHladjenja() {
        return minimalnaBrzinaHladjenja;
    }
    public void setMinimalnaBrzinaHladjenja(String v) {
        this.minimalnaBrzinaHladjenja = v;
    }

    public String getProsjecnaBrzinaHladjenja() {
        return prosjecnaBrzinaHladjenja;
    }
    public void setProsjecnaBrzinaHladjenja(String v) {
        this.prosjecnaBrzinaHladjenja = v;
    }

    public int getGKodId() {
        return gKodId;
    }
    public void setGKodId(int id) {
        this.gKodId = id;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
