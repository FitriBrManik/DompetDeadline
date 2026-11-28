import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import repository.KategoriRepository;
import repository.TransaksiRepository;
import repository.TugasRepository;
import service.KategoriService;
import service.TransaksiService;
import service.TugasService;
import view.MainFrame;
import view.TransaksiPanel;
import view.TugasPanel;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                KategoriRepository kategoriRepository = new KategoriRepository();
                TugasRepository tugasRepository = new TugasRepository();
                TransaksiRepository transaksiRepository = new TransaksiRepository();

                KategoriService kategoriService = new KategoriService(kategoriRepository);
                TugasService tugasService = new TugasService(tugasRepository);
                TransaksiService transaksiService = new TransaksiService(transaksiRepository, kategoriRepository);

                MainFrame mainFrame = new MainFrame();
                mainFrame.setPanelTugas(new TugasPanel(tugasService));
                mainFrame.setPanelTransaksi(new TransaksiPanel(transaksiService, kategoriService));
                mainFrame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Gagal menjalankan aplikasi: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}