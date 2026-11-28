package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Kategori;
import model.Transaksi;
import model.Tugas;
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
        validasiInput(tanggal, jumlah, kategoriId);
        String catatanBersih = catatan == null ? "" : catatan.trim();
        Transaksi transaksi = new Transaksi(0, tanggal, jumlah, kategoriId, tugasId, catatanBersih);
        return transaksiRepository.simpan(transaksi);
    }

    public boolean ubahTransaksi(
            int id,
            LocalDate tanggal,
            BigDecimal jumlah,
            int kategoriId,
            Integer tugasId,
            String catatan) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID transaksi tidak valid.");
        }
        validasiInput(tanggal, jumlah, kategoriId);
        String catatanBersih = catatan == null ? "" : catatan.trim();
        Transaksi transaksi = new Transaksi(id, tanggal, jumlah, kategoriId, tugasId, catatanBersih);
        return transaksiRepository.perbarui(transaksi);
    }

    public List<Transaksi> ambilSemuaTransaksi() throws SQLException {
        return transaksiRepository.ambilSemua();
    }

    public Map<Integer, BigDecimal> hitungTotalBiayaPerTugas() throws SQLException {
        List<Transaksi> daftarTransaksi = transaksiRepository.ambilSemua();
        Map<Integer, BigDecimal> totalPerTugas = new HashMap<>();

        for (Transaksi tr : daftarTransaksi) {
            if (tr.getTugasId() != null) {
                BigDecimal saatIni = totalPerTugas.getOrDefault(tr.getTugasId(), BigDecimal.ZERO);
                totalPerTugas.put(tr.getTugasId(), saatIni.add(tr.getJumlah()));
            }
        }
        return totalPerTugas;
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

    public BigDecimal hitungSisaUangBulan(YearMonth bulan) throws SQLException {
        List<Transaksi> daftarTransaksi = transaksiRepository.ambilSemua();
        List<Kategori> daftarKategori = kategoriRepository.ambilSemua();

        Map<Integer, String> jenisPerKategori = new HashMap<>();
        for (Kategori k : daftarKategori) {
            jenisPerKategori.put(k.getId(), k.getJenis() == null ? "" : k.getJenis().toLowerCase());
        }

        BigDecimal pemasukan = BigDecimal.ZERO;
        BigDecimal pengeluaran = BigDecimal.ZERO;
        for (Transaksi t : daftarTransaksi) {
            if (t.getTanggal() != null && YearMonth.from(t.getTanggal()).equals(bulan)) {
                String jenis = jenisPerKategori.getOrDefault(t.getKategoriId(), "");
                if ("pemasukan".equals(jenis)) {
                    pemasukan = pemasukan.add(t.getJumlah());
                } else if ("pengeluaran".equals(jenis)) {
                    pengeluaran = pengeluaran.add(t.getJumlah());
                }
            }
        }
        return pemasukan.subtract(pengeluaran);
    }

    public BigDecimal hitungTotalBiayaTugasAktif(List<Tugas> daftarTugas) throws SQLException {
        Map<Integer, BigDecimal> biayaPerTugas = hitungTotalBiayaPerTugas();
        BigDecimal totalAktif = BigDecimal.ZERO;
        for (Tugas tugas : daftarTugas) {
            if (!tugas.isSelesai()) {
                totalAktif = totalAktif.add(biayaPerTugas.getOrDefault(tugas.getId(), BigDecimal.ZERO));
            }
        }
        return totalAktif;
    }

    public Map<String, BigDecimal> hitungPengeluaranPerKategoriBulan(YearMonth bulan) throws SQLException {
        List<Transaksi> daftarTransaksi = transaksiRepository.ambilSemua();
        List<Kategori> daftarKategori = kategoriRepository.ambilSemua();

        Map<Integer, Kategori> petaKategori = new HashMap<>();
        for (Kategori k : daftarKategori) {
            petaKategori.put(k.getId(), k);
        }

        Map<String, BigDecimal> hasil = new LinkedHashMap<>();
        for (Transaksi t : daftarTransaksi) {
            if (t.getTanggal() != null && YearMonth.from(t.getTanggal()).equals(bulan)) {
                Kategori k = petaKategori.get(t.getKategoriId());
                if (k != null && "pengeluaran".equalsIgnoreCase(k.getJenis())) {
                    String namaKategori = k.getNama();
                    BigDecimal nilaiLama = hasil.getOrDefault(namaKategori, BigDecimal.ZERO);
                    hasil.put(namaKategori, nilaiLama.add(t.getJumlah()));
                }
            }
        }
        return hasil;
    }

    public boolean hapusTransaksi(int id) throws SQLException {
        return transaksiRepository.hapus(id);
    }

    private void validasiInput(LocalDate tanggal, BigDecimal jumlah, int kategoriId) {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal transaksi tidak boleh kosong.");
        }
        if (jumlah == null || jumlah.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih besar dari 0.");
        }
        if (kategoriId <= 0) {
            throw new IllegalArgumentException("Kategori transaksi wajib dipilih.");
        }
    }
}