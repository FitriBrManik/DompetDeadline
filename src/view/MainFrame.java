package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    private final JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Dompet Deadline");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buatHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.addTab("Tugas & Deadline", buatPlaceholderPanel("Panel Tugas & Deadline segera hadir"));
        tabbedPane.addTab("Dompet & Transaksi", buatPlaceholderPanel("Panel Dompet & Transaksi segera hadir"));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel judul = new JLabel("Dompet Deadline");
        judul.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subjudul = new JLabel("Kelola tenggat tugas dan catatan keuanganmu");
        subjudul.setFont(new Font("SansSerif", Font.PLAIN, 12));

        header.add(judul, BorderLayout.NORTH);
        header.add(subjudul, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buatPlaceholderPanel(String teks) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel label = new JLabel(teks, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void setPanelTugas(JPanel panelTugas) {
        tabbedPane.setComponentAt(0, panelTugas);
    }

    public void setPanelTransaksi(JPanel panelTransaksi) {
        tabbedPane.setComponentAt(1, panelTransaksi);
    }
}