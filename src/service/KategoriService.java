package service;

import java.sql.SQLException;
import java.util.List;
import model.Kategori;
import repository.KategoriRepository;

public class KategoriService {

    private final KategoriRepository kategoriRepository;

    public KategoriService(KategoriRepository kategoriRepository) {
        this.kategoriRepository = kategoriRepository;
    }

    public Kategori tambahKategori(String nama, String jenis) throws SQLException {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama kategori tidak boleh kosong.");
        }
        String jenisNormal = jenis == null ? "" : jenis.trim().toLowerCase();
        if (!jenisNormal.equals("pemasukan") && !jenisNormal.equals("pengeluaran")) {
            throw new IllegalArgumentException("Jenis kategori harus 'pemasukan' atau 'pengeluaran'.");
        }

        Kategori kategori = new Kategori(0, nama.trim(), jenisNormal);
        return kategoriRepository.simpan(kategori);
    }

    public List<Kategori> ambilSemuaKategori() throws SQLException {
        return kategoriRepository.ambilSemua();
    }

    public boolean hapusKategori(int id) throws SQLException {
        return kategoriRepository.hapus(id);
    }
}