package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Transaksi;
import repository.TransaksiRepository;

public class TransaksiService {

    private final TransaksiRepository transaksiRepository;

    public TransaksiService() {
        this.transaksiRepository = new TransaksiRepository();
    }

    public TransaksiService(TransaksiRepository transaksiRepository) {
        this.transaksiRepository = transaksiRepository;
    }

    public Transaksi tambahTransaksi(String jenis, double jumlah, String catatan, LocalDate tanggal, int kategoriId)
            throws SQLException {
        if (jenis == null || jenis.isBlank()) {
            throw new IllegalArgumentException("Jenis transaksi tidak boleh kosong.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih dari 0.");
        }
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal transaksi wajib diisi.");
        }

        Transaksi transaksi = new Transaksi(
                jenis.trim(),
                jumlah,
                catatan == null ? "" : catatan.trim(),
                tanggal,
                kategoriId
        );
        return transaksiRepository.simpan(transaksi);
    }

    public List<Transaksi> ambilSemuaTransaksi() throws SQLException {
        return transaksiRepository.ambilSemua();
    }

    public double hitungSaldo() throws SQLException {
        List<Transaksi> daftar = transaksiRepository.ambilSemua();
        double saldo = 0;
        for (Transaksi t : daftar) {
            if ("PEMASUKAN".equalsIgnoreCase(t.getJenis())) {
                saldo += t.getJumlah();
            } else if ("PENGELUARAN".equalsIgnoreCase(t.getJenis())) {
                saldo -= t.getJumlah();
            }
        }
        return saldo;
    }

    public boolean hapusTransaksi(int id) throws SQLException {
        return transaksiRepository.hapus(id);
    }
}
