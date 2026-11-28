package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Kategori;
import model.Transaksi;
import repository.KategoriRepository;
import repository.TransaksiRepository;

public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final KategoriRepository kategoriRepository;

    public TransaksiService(TransaksiRepository transaksiRepository, KategoriRepository kategoriRepository) {
        this.transaksiRepository = transaksiRepository;
        this.kategoriRepository = kategoriRepository;
    }

    public Transaksi tambahTransaksi(
            LocalDate tanggal,
            BigDecimal jumlah,
            int kategoriId,
            Integer tugasId,
            String catatan) throws SQLException {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal transaksi tidak boleh kosong.");
        }
        if (jumlah == null || jumlah.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih besar dari 0.");
        }
        if (kategoriId <= 0) {
            throw new IllegalArgumentException("Kategori transaksi wajib dipilih.");
        }

        String catatanBersih = catatan == null ? "" : catatan.trim();
        Transaksi transaksi = new Transaksi(0, tanggal, jumlah, kategoriId, tugasId, catatanBersih);
        return transaksiRepository.simpan(transaksi);
    }

    public List<Transaksi> ambilSemuaTransaksi() throws SQLException {
        return transaksiRepository.ambilSemua();
    }

    public BigDecimal hitungSaldo() throws SQLException {
        List<Transaksi> daftarTransaksi = transaksiRepository.ambilSemua();
        List<Kategori> daftarKategori = kategoriRepository.ambilSemua();

        Map<Integer, String> jenisPerKategori = new HashMap<>();
        for (Kategori k : daftarKategori) {
            jenisPerKategori.put(k.getId(), k.getJenis() == null ? "" : k.getJenis().toLowerCase());
        }

        BigDecimal saldo = BigDecimal.ZERO;
        for (Transaksi t : daftarTransaksi) {
            String jenis = jenisPerKategori.getOrDefault(t.getKategoriId(), "");
            if ("pemasukan".equals(jenis)) {
                saldo = saldo.add(t.getJumlah());
            } else if ("pengeluaran".equals(jenis)) {
                saldo = saldo.subtract(t.getJumlah());
            }
        }
        return saldo;
    }

    public boolean hapusTransaksi(int id) throws SQLException {
        return transaksiRepository.hapus(id);
    }
}