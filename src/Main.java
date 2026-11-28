import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import repository.KategoriRepository;
import repository.KoneksiDatabase;
import repository.TransaksiRepository;
import repository.TugasRepository;
import service.CsvExportService;
import service.KategoriService;
import service.PengingatTenggatService;
import service.TransaksiService;
import service.TugasService;
import view.DashboardPanel;
import view.TransaksiPanel;
import view.TugasPanel;

public class Main {

    public static void main(String[] args) {
        KoneksiDatabase.inisialisasiTabel();

        KategoriRepository kategoriRepository = new KategoriRepository();
        TugasRepository tugasRepository = new TugasRepository();
        TransaksiRepository transaksiRepository = new TransaksiRepository();

        KategoriService kategoriService = new KategoriService(kategoriRepository);
        TugasService tugasService = new TugasService(tugasRepository);
        TransaksiService transaksiService = new TransaksiService(transaksiRepository, kategoriRepository);
        CsvExportService csvExportService = new CsvExportService(transaksiRepository, kategoriRepository, tugasRepository);
        PengingatTenggatService pengingatService = new PengingatTenggatService(tugasService);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dompet Deadline");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(960, 640);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            DashboardPanel dashboardPanel = new DashboardPanel(transaksiService, tugasService);
            TugasPanel tugasPanel = new TugasPanel(tugasService, transaksiService);
            TransaksiPanel transaksiPanel = new TransaksiPanel(transaksiService, kategoriService, tugasService);
            transaksiPanel.setCsvExportService(csvExportService);

            tugasPanel.setOnDataBerubah(() -> {
                dashboardPanel.muatUlangDashboard();
                transaksiPanel.muatDaftarTugas();
                transaksiPanel.muatDataTransaksi();
            });

            transaksiPanel.setOnDataBerubah(() -> {
                dashboardPanel.muatUlangDashboard();
                tugasPanel.muatDataTugas();
            });

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Dashboard", dashboardPanel);
            tabbedPane.addTab("Tugas", tugasPanel);
            tabbedPane.addTab("Transaksi", transaksiPanel);

            frame.add(tabbedPane, BorderLayout.CENTER);

            pengingatService.mulai(3000L, 60000L);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    pengingatService.hentikan();
                }
            });

            frame.setVisible(true);
        });
    }
}