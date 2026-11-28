package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Transaksi;

public class TransaksiRepository {

    public Transaksi simpan(Transaksi transaksi) throws SQLException {
        String sql = """
                INSERT INTO transaksi (tanggal, jumlah, kategori_id, tugas_id, catatan)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaksi.getTanggal().toString());
            stmt.setLong(2, transaksi.getJumlah().longValueExact());
            stmt.setInt(3, transaksi.getKategoriId());

            if (transaksi.getTugasId() != null) {
                stmt.setInt(4, transaksi.getTugasId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, transaksi.getCatatan());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    transaksi.setId(keys.getInt(1));
                }
            }
            return transaksi;
        }
    }

    public List<Transaksi> ambilSemua() throws SQLException {
        String sql = """
                SELECT id, tanggal, jumlah, kategori_id, tugas_id, catatan
                FROM transaksi
                ORDER BY tanggal DESC, id DESC
                """;
        List<Transaksi> daftar = new ArrayList<>();

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int tugasVal = rs.getInt("tugas_id");
                Integer tugasId = rs.wasNull() ? null : tugasVal;

                Transaksi tr = new Transaksi(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("tanggal")),
                        BigDecimal.valueOf(rs.getLong("jumlah")),
                        rs.getInt("kategori_id"),
                        tugasId,
                        rs.getString("catatan")
                );
                daftar.add(tr);
            }
        }
        return daftar;
    }

    public boolean hapus(int id) throws SQLException {
        String sql = "DELETE FROM transaksi WHERE id = ?";

        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
