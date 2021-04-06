package com.example.park_app;

public class getKendaraan {
    String plat, merk, key;

    public getKendaraan() {

    }

    public getKendaraan(String plat, String merk, String key) {
        this.plat = plat;
        this.merk = merk;
        this.key = key;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
