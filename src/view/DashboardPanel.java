package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.Tugas;
import service.TransaksiService;
import service.TugasService;

public class DashboardPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final TugasService tugasService;

    private final JLabel lblSisaBulan = new JLabel("Rp 0");
    private final JLabel lblBiayaTugasAktif = new JLabel("Rp 0");
    private final JLabel lblTenggatTerdekat = new JLabel("<html>- Belum ada tugas aktif -</html>");

    private final JComboBox<YearMonth> cmbBulan;
    private final GrafikPengeluaranPanel grafikPanel = new GrafikPengeluaranPanel();
    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public DashboardPanel(TransaksiService transaksiService, TugasService tugasService) {
        this.transaksiService = transaksiService;
        this.tugasService = tugasService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        formatRupiah.setMaximumFractionDigits(0);

        YearMonth bulanSekarang = YearMonth.now();
        cmbBulan = new JComboBox<>(new YearMonth[]{
            bulanSekarang.minusMonths(2),
            bulanSekarang.minusMonths(1),
            bulanSekarang,
            bulanSekarang.plusMonths(1)
        });
        cmbBulan.setSelectedItem(bulanSekarang);
        cmbBulan.addActionListener(e -> muatUlangDashboard());

        add(buatPanelKartuRingkasan(), BorderLayout.NORTH);
        add(buatPanelGrafik(), BorderLayout.CENTER);

        muatUlangDashboard();
    }

    private JPanel buatPanelKartuRingkasan() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 10));

        JPanel kartuSisa = new JPanel(new BorderLayout());
        kartuSisa.setBorder(BorderFactory.createTitledBorder("Sisa Uang Bulan Berjalan"));
        lblSisaBulan.setFont(new Font("SansSerif", Font.BOLD, 16));
        kartuSisa.add(lblSisaBulan, BorderLayout.CENTER);

        JPanel kartuBiayaAktif = new JPanel(new BorderLayout());
        kartuBiayaAktif.setBorder(BorderFactory.createTitledBorder("Total Biaya Tugas Aktif"));
        lblBiayaTugasAktif.setFont(new Font("SansSerif", Font.BOLD, 16));
        kartuBiayaAktif.add(lblBiayaTugasAktif, BorderLayout.CENTER);

        JPanel kartuTenggat = new JPanel(new BorderLayout());
        kartuTenggat.setBorder(BorderFactory.createTitledBorder("3 Tenggat Terdekat (Belum Selesai)"));
        lblTenggatTerdekat.setFont(new Font("SansSerif", Font.PLAIN, 12));
        kartuTenggat.add(lblTenggatTerdekat, BorderLayout.CENTER);

        grid.add(kartuSisa);
        grid.add(kartuBiayaAktif);
        grid.add(kartuTenggat);
        return grid;
    }

    private JPanel buatPanelGrafik() {
        JPanel container = new JPanel(new BorderLayout(8, 8));
        container.setBorder(BorderFactory.createTitledBorder("Grafik Pengeluaran per Kategori"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Bulan:"));
        toolbar.add(cmbBulan);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatUlangDashboard());
        toolbar.add(btnRefresh);

        container.add(toolbar, BorderLayout.NORTH);
        container.add(grafikPanel, BorderLayout.CENTER);
        return container;
    }

    public void muatUlangDashboard() {
        try {
            YearMonth bulanDipilih = (YearMonth) cmbBulan.getSelectedItem();
            if (bulanDipilih == null) {
                bulanDipilih = YearMonth.now();
            }

            BigDecimal sisaBulan = transaksiService.hitungSisaUangBulan(bulanDipilih);
            lblSisaBulan.setText(formatRupiah.format(sisaBulan));

            List<Tugas> semuaTugas = tugasService.ambilSemuaTugas();
            BigDecimal biayaAktif = transaksiService.hitungTotalBiayaTugasAktif(semuaTugas);
            lblBiayaTugasAktif.setText(formatRupiah.format(biayaAktif));

            List<Tugas> terdekat = semuaTugas.stream()
                    .filter(t -> !t.isSelesai() && t.getTenggat() != null)
                    .sorted(Comparator.comparing(Tugas::getTenggat))
                    .limit(3)
                    .collect(Collectors.toList());

            if (terdekat.isEmpty()) {
                lblTenggatTerdekat.setText("<html>- Tidak ada tugas aktif -</html>");
            } else {
                LocalDate hariIni = LocalDate.now();
                StringBuilder sb = new StringBuilder("<html>");
                for (int i = 0; i < terdekat.size(); i++) {
                    Tugas t = terdekat.get(i);
                    String penanda = t.getTenggat().isBefore(hariIni) ? " [Terlewat]" : "";
                    sb.append(i + 1)
                      .append(". ")
                      .append(escapeHtml(t.getJudul()))
                      .append(" (")
                      .append(t.getTenggat())
                      .append(")")
                      .append(penanda);
                    if (i < terdekat.size() - 1) {
                        sb.append("<br>");
                    }
                }
                sb.append("</html>");
                lblTenggatTerdekat.setText(sb.toString());
            }

            Map<String, BigDecimal> dataGrafik = transaksiService.hitungPengeluaranPerKategoriBulan(bulanDipilih);
            grafikPanel.setDataKategori(dataGrafik);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal memuat data dashboard: " + ex.getMessage(),
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private String escapeHtml(String teks) {
        if (teks == null) {
            return "";
        }
        return teks.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}