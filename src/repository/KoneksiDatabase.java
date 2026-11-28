package repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class KoneksiDatabase {
    private static final Path DB_PATH = Paths.get(System.getProperty("user.home"), "dompet_deadline.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void inisialisasiTabel() {
        String sqlKategori = """
                CREATE TABLE IF NOT EXISTS kategori (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    jenis TEXT NOT NULL CHECK (jenis IN ('pemasukan', 'pengeluaran'))
                );
                """;

        String sqlTugas = """
                CREATE TABLE IF NOT EXISTS tugas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    judul TEXT NOT NULL,
                    mata_kuliah TEXT NOT NULL,
                    tenggat TEXT NOT NULL,
                    prioritas TEXT NOT NULL CHECK (prioritas IN ('tinggi', 'sedang', 'rendah')),
                    selesai INTEGER NOT NULL DEFAULT 0
                );
                """;

        String sqlTransaksi = """
                CREATE TABLE IF NOT EXISTS transaksi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tanggal TEXT NOT NULL,
                    jumlah INTEGER NOT NULL,
                    kategori_id INTEGER NOT NULL,
                    tugas_id INTEGER,
                    catatan TEXT,
                    FOREIGN KEY (kategori_id) REFERENCES kategori(id),
                    FOREIGN KEY (tugas_id) REFERENCES tugas(id)
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sqlKategori);
            stmt.execute(sqlTugas);
            stmt.execute(sqlTransaksi);
        } catch (SQLException e) {
            System.err.println("Gagal menginisialisasi database: " + e.getMessage());
        }
    }
}