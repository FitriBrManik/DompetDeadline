package service;

import java.sql.SQLException;
import java.util.List;
import model.Kategori;
import repository.KategoriRepository;

public class KategoriService {

    private final KategoriRepository kategoriRepository;

    public KategoriService() {
        this.kategoriRepository = new KategoriRepository();
    }

    public KategoriService(KategoriRepository kategoriRepository) {
        this.kategoriRepository = kategoriRepository;
    }

    public Kategori tambahKategori(String nama, String jenis) throws SQLException {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama kategori tidak boleh kosong.");
        }
        if (jenis == null || jenis.isBlank()) {
            throw new IllegalArgumentException("Jenis kategori tidak boleh kosong.");
        }

        Kategori kategori = new Kategori(nama.trim(), jenis.trim());
        return kategoriRepository.simpan(kategori);
    }

    public List<Kategori> ambilSemuaKategori() throws SQLException {
        return kategoriRepository.ambilSemua();
    }

    public boolean hapusKategori(int id) throws SQLException {
        return kategoriRepository.hapus(id);
    }
}