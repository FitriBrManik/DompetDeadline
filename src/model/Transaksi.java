package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaksi {
    private int id;
    private LocalDate tanggal;
    private BigDecimal jumlah;
    private int kategoriId;
    private Integer tugasId;
    private String catatan;

    public Transaksi() {
    }

    public Transaksi(int id, LocalDate tanggal, BigDecimal jumlah, int kategoriId, Integer tugasId, String catatan) {
        this.id = id;
        this.tanggal = tanggal;
        this.jumlah = jumlah;
        this.kategoriId = kategoriId;
        this.tugasId = tugasId;
        this.catatan = catatan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public BigDecimal getJumlah() {
        return jumlah;
    }

    public void setJumlah(BigDecimal jumlah) {
        this.jumlah = jumlah;
    }

    public int getKategoriId() {
        return kategoriId;
    }

    public void setKategoriId(int kategoriId) {
        this.kategoriId = kategoriId;
    }

    public Integer getTugasId() {
        return tugasId;
    }

    public void setTugasId(Integer tugasId) {
        this.tugasId = tugasId;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}