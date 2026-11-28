package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Tugas;
import repository.TugasRepository;

public class TugasService {

    private final TugasRepository tugasRepository;

    public TugasService(TugasRepository tugasRepository) {
        this.tugasRepository = tugasRepository;
    }

    public Tugas tambahTugas(String judul, String mataKuliah, LocalDate tenggat, String prioritas)
            throws SQLException {
        validasiInput(judul, mataKuliah, tenggat, prioritas);
        Tugas tugas = new Tugas(
                0,
                judul.trim(),
                mataKuliah.trim(),
                tenggat,
                normalisasiPrioritas(prioritas),
                false
        );
        return tugasRepository.simpan(tugas);
    }

    public boolean ubahTugas(
            int id,
            String judul,
            String mataKuliah,
            LocalDate tenggat,
            String prioritas,
            boolean selesai) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID tugas tidak valid.");
        }
        validasiInput(judul, mataKuliah, tenggat, prioritas);
        Tugas tugas = new Tugas(
                id,
                judul.trim(),
                mataKuliah.trim(),
                tenggat,
                normalisasiPrioritas(prioritas),
                selesai
        );
        return tugasRepository.perbarui(tugas);
    }

    public List<Tugas> ambilSemuaTugas() throws SQLException {
        return tugasRepository.ambilSemua();
    }

    public boolean tandaiSelesai(int id) throws SQLException {
        return tugasRepository.perbaruiStatusSelesai(id, true);
    }

    public boolean perbaruiStatusSelesai(int id, boolean selesai) throws SQLException {
        return tugasRepository.perbaruiStatusSelesai(id, selesai);
    }

    public boolean hapusTugas(int id) throws SQLException {
        return tugasRepository.hapus(id);
    }

    private void validasiInput(String judul, String mataKuliah, LocalDate tenggat, String prioritas) {
        if (judul == null || judul.isBlank()) {
            throw new IllegalArgumentException("Judul tugas tidak boleh kosong.");
        }
        if (mataKuliah == null || mataKuliah.isBlank()) {
            throw new IllegalArgumentException("Mata kuliah tidak boleh kosong.");
        }
        if (tenggat == null) {
            throw new IllegalArgumentException("Tanggal tenggat tidak boleh kosong.");
        }
        normalisasiPrioritas(prioritas);
    }

    private String normalisasiPrioritas(String prioritas) {
        String prioritasNormal = (prioritas == null || prioritas.isBlank())
                ? "sedang"
                : prioritas.trim().toLowerCase();
        if (!prioritasNormal.equals("tinggi")
                && !prioritasNormal.equals("sedang")
                && !prioritasNormal.equals("rendah")) {
            throw new IllegalArgumentException("Prioritas harus 'tinggi', 'sedang', atau 'rendah'.");
        }
        return prioritasNormal;
    }
}