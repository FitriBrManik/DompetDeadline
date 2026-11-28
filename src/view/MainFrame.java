package view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private DashboardPanel panelDashboard;
    private TugasPanel panelTugas;
    private TransaksiPanel panelTransaksi;

    public MainFrame() {
        setTitle("Dompet Deadline");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setPanelDashboard(DashboardPanel panelDashboard) {
        this.panelDashboard = panelDashboard;
        rebuildTabs();
    }

    public void setPanelTugas(TugasPanel panelTugas) {
        this.panelTugas = panelTugas;
        rebuildTabs();
    }

    public void setPanelTransaksi(TransaksiPanel panelTransaksi) {
        this.panelTransaksi = panelTransaksi;
        rebuildTabs();
    }

    private void rebuildTabs() {
        int indexAktif = tabbedPane.getSelectedIndex();
        tabbedPane.removeAll();

        if (panelDashboard != null) {
            tabbedPane.addTab("Dashboard", panelDashboard);
        }
        if (panelTugas != null) {
            tabbedPane.addTab("Tugas", panelTugas);
        }
        if (panelTransaksi != null) {
            tabbedPane.addTab("Transaksi", panelTransaksi);
        }

        if (indexAktif >= 0 && indexAktif < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(indexAktif);
        }
    }
}