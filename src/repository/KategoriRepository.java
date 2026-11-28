package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Kategori;

public class KategoriRepository {

    public Kategori simpan(Kategori kategori) throws SQLException {
        String sql = "INSERT INTO kategori (nama, jenis) VALUES (?, ?)";

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, kategori.getNama());
            stmt.setString(2, kategori.getJenis());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    kategori.setId(keys.getInt(1));
                }
            }
            return kategori;
        }
    }

    public List<Kategori> ambilSemua() throws SQLException {
        String sql = "SELECT id, nama, jenis FROM kategori ORDER BY id ASC";
        List<Kategori> daftar = new ArrayList<>();

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Kategori k = new Kategori(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("jenis")
                );
                daftar.add(k);
            }
        }
        return daftar;
    }

    public boolean hapus(int id) throws SQLException {
        String sql = "DELETE FROM kategori WHERE id = ?";

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}