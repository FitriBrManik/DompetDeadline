package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Tugas;
import repository.TugasRepository;

public class TugasService {

    private final TugasRepository tugasRepository;

    public TugasService() {
        this.tugasRepository = new TugasRepository();
    }

    public TugasService(TugasRepository tugasRepository) {
        this.tugasRepository = tugasRepository;
    }

    public Tugas tambahTugas(String judul, String deskripsi, LocalDate tenggat, int kategoriId) throws SQLException {
        if (judul == null || judul.isBlank()) {
            throw new IllegalArgumentException("Judul tugas tidak boleh kosong.");
        }
        if (tenggat == null) {
            throw new IllegalArgumentException("Tanggal tenggat tugas wajib diisi.");
        }

        Tugas tugas = new Tugas(judul.trim(), deskripsi == null ? "" : deskripsi.trim(), tenggat, kategoriId);
        return tugasRepository.simpan(tugas);
    }

    public List<Tugas> ambilSemuaTugas() throws SQLException {
        return tugasRepository.ambilSemua();
    }

    public boolean tandaiSelesai(int id) throws SQLException {
        return tugasRepository.updateStatus(id, true);
    }

    public boolean hapusTugas(int id) throws SQLException {
        return tugasRepository.hapus(id);
    }
}}
