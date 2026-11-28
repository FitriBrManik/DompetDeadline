package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Tugas;

public class TugasRepository {

    public Tugas simpan(Tugas tugas) throws SQLException {
        String sql = """
                INSERT INTO tugas (judul, mata_kuliah, tenggat, prioritas, selesai)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tugas.getJudul());
            stmt.setString(2, tugas.getMataKuliah());
            stmt.setString(3, tugas.getTenggat().toString());
            stmt.setString(4, tugas.getPrioritas());
            stmt.setInt(5, tugas.isSelesai() ? 1 : 0);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tugas.setId(keys.getInt(1));
                }
            }
            return tugas;
        }
    }

    public List<Tugas> ambilSemua() throws SQLException {
        String sql = """
                SELECT id, judul, mata_kuliah, tenggat, prioritas, selesai
                FROM tugas
                ORDER BY selesai ASC, tenggat ASC, id ASC
                """;
        List<Tugas> daftar = new ArrayList<>();

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Tugas t = new Tugas(
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("mata_kuliah"),
                        LocalDate.parse(rs.getString("tenggat")),
                        rs.getString("prioritas"),
                        rs.getInt("selesai") == 1
                );
                daftar.add(t);
            }
        }
        return daftar;
    }

    public boolean perbaruiStatusSelesai(int id, boolean selesai) throws SQLException {
        String sql = "UPDATE tugas SET selesai = ? WHERE id = ?";

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selesai ? 1 : 0);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean hapus(int id) throws SQLException {
        String sql = "DELETE FROM tugas WHERE id = ?";

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
