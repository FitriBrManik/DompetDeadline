package model;

import java.time.LocalDate;

public class Tugas {
    private int id;
    private String judul;
    private String mataKuliah;
    private LocalDate tenggat;
    private String prioritas; // "tinggi", "sedang", atau "rendah"
    private boolean selesai;

    public Tugas() {
    }

    public Tugas(int id, String judul, String mataKuliah, LocalDate tenggat, String prioritas, boolean selesai) {
        this.id = id;
        this.judul = judul;
        this.mataKuliah = mataKuliah;
        this.tenggat = tenggat;
        this.prioritas = prioritas;
        this.selesai = selesai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getMataKuliah() {
        return mataKuliah;
    }

    public void setMataKuliah(String mataKuliah) {
        this.mataKuliah = mataKuliah;
    }

    public LocalDate getTenggat() {
        return tenggat;
    }

    public void setTenggat(LocalDate tenggat) {
        this.tenggat = tenggat;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public boolean isSelesai() {
        return selesai;
    }

    public void setSelesai(boolean selesai) {
        this.selesai = selesai;
    }
}