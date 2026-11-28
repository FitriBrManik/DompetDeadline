package service;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.Tugas;

public class PengingatTenggatService {

    private final TugasService tugasService;
    private final Set<Integer> idSudahDiingatkan = new HashSet<>();
    private Timer timer;
    private TrayIcon trayIcon;

    public PengingatTenggatService(TugasService tugasService) {
        this.tugasService = tugasService;
        inisialisasiSystemTray();
    }

    private void inisialisasiSystemTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image gambar = buatIkonSederhana();
            trayIcon = new TrayIcon(gambar, "Dompet Deadline");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (AWTException ex) {
            trayIcon = null;
        }
    }

    private Image buatIkonSederhana() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean bingkai = x == 0 || y == 0 || x == 15 || y == 15;
                img.setRGB(x, y, bingkai ? 0xFF1E3A8A : 0xFF3B82F6);
            }
        }
        return img;
    }

    public void mulai(long jedaAwalMs, long intervalMs) {
        hentikan();
        timer = new Timer("PengingatTenggatTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cekTenggatMendesak();
            }
        }, jedaAwalMs, intervalMs);
    }

    public void cekTenggatMendesak() {
        try {
            List<Tugas> daftarTugas = tugasService.ambilSemuaTugas();
            LocalDateTime sekarang = LocalDateTime.now();
            LocalDateTime batas24Jam = sekarang.plusHours(24);

            for (Tugas tugas : daftarTugas) {
                if (tugas.isSelesai() || tugas.getTenggat() == null) {
                    continue;
                }
                if (idSudahDiingatkan.contains(tugas.getId())) {
                    continue;
                }
                LocalDateTime tenggatAkhirHari = tugas.getTenggat().atTime(23, 59, 59);
                if (!tenggatAkhirHari.isBefore(sekarang) && !tenggatAkhirHari.isAfter(batas24Jam)) {
                    idSudahDiingatkan.add(tugas.getId());
                    tampilkanNotifikasi(tugas);
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void tampilkanNotifikasi(Tugas tugas) {
        String judulPesan = "Pengingat Tenggat Tugas";
        String isiPesan = tugas.getJudul()
                + " (" + tugas.getMataKuliah() + ") - Tenggat: "
                + tugas.getTenggat();

        if (trayIcon != null) {
            trayIcon.displayMessage(judulPesan, isiPesan, TrayIcon.MessageType.WARNING);
            return;
        }

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                isiPesan,
                judulPesan,
                JOptionPane.WARNING_MESSAGE
        ));
    }

    public void hentikan() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}