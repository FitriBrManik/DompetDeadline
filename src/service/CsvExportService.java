package service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Kategori;
import model.Transaksi;
import model.Tugas;
import repository.KategoriRepository;
import repository.TransaksiRepository;
import repository.TugasRepository;

public class CsvExportService {

    private final TransaksiRepository transaksiRepository;
    private final KategoriRepository kategoriRepository;
    private final TugasRepository tugasRepository;

    public CsvExportService(
            TransaksiRepository transaksiRepository,
            KategoriRepository kategoriRepository,
            TugasRepository tugasRepository) {
        this.transaksiRepository = transaksiRepository;
        this.kategoriRepository = kategoriRepository;
        this.tugasRepository = tugasRepository;
    }

    public void eksporTransaksiKeCsv(File tujuan) throws SQLException, IOException {
        List<Transaksi> daftarTransaksi = transaksiRepository.ambilSemua();
        List<Kategori> daftarKategori = kategoriRepository.ambilSemua();
        List<Tugas> daftarTugas = tugasRepository.ambilSemua();

        Map<Integer, Kategori> petaKategori = new HashMap<>();
        for (Kategori k : daftarKategori) {
            petaKategori.put(k.getId(), k);
        }

        Map<Integer, Tugas> petaTugas = new HashMap<>();
        for (Tugas t : daftarTugas) {
            petaTugas.put(t.getId(), t);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tujuan), StandardCharsets.UTF_8))) {
            writer.write("ID,Tanggal,Kategori,Jenis,Jumlah,Tugas,Catatan");
            writer.newLine();

            for (Transaksi tr : daftarTransaksi) {
                Kategori kat = petaKategori.get(tr.getKategoriId());
                String namaKat = kat != null ? kat.getNama() : ("ID " + tr.getKategoriId());
                String jenisKat = kat != null ? kat.getJenis() : "-";

                String judulTugas = "-";
                if (tr.getTugasId() != null) {
                    Tugas tg = petaTugas.get(tr.getTugasId());
                    judulTugas = tg != null ? tg.getJudul() : ("ID " + tr.getTugasId());
                }

                String baris = String.join(",",
                        escapeCsv(String.valueOf(tr.getId())),
                        escapeCsv(tr.getTanggal() != null ? tr.getTanggal().toString() : ""),
                        escapeCsv(namaKat),
                        escapeCsv(jenisKat),
                        escapeCsv(tr.getJumlah() != null ? tr.getJumlah().toPlainString() : "0"),
                        escapeCsv(judulTugas),
                        escapeCsv(tr.getCatatan() != null ? tr.getCatatan() : "")
                );
                writer.write(baris);
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String nilai) {
        if (nilai == null) {
            return "\"\"";
        }
        String bersih = nilai.replace("\"", "\"\"");
        return "\"" + bersih + "\"";
    }
}