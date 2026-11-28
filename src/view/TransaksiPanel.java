package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Kategori;
import model.Transaksi;
import service.KategoriService;
import service.TransaksiService;

public class TransaksiPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;

    private final JTextField txtTanggal = new JTextField(12);
    private final JTextField txtJumlah = new JTextField(14);
    private final JComboBox<KategoriItem> cmbKategori = new JComboBox<>();
    private final JTextField txtCatatan = new JTextField(22);

    private final JLabel lblSaldo = new JLabel("Saldo: Rp 0");
    private final DefaultTableModel tableModel;
    private final JTable tableTransaksi;
    private List<Transaksi> daftarTransaksi = new ArrayList<>();

    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public TransaksiPanel(TransaksiService transaksiService, KategoriService kategoriService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        txtTanggal.setText(LocalDate.now().toString());
        formatRupiah.setMaximumFractionDigits(0);

        add(buatPanelAtas(), BorderLayout.NORTH);

        String[] kolom = {"ID", "Tanggal", "Kategori", "Jenis", "Jumlah", "Catatan"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTransaksi = new JTable(tableModel);
        add(new JScrollPane(tableTransaksi), BorderLayout.CENTER);

        add(buatAksiPanel(), BorderLayout.SOUTH);

        muatKategori();
        muatDataTransaksi();
    }

    private JPanel buatPanelAtas() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));

        JPanel panelSaldo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSaldo.setBorder(BorderFactory.createTitledBorder("Ringkasan Dompet"));
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSaldo.add(lblSaldo);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Tambah Transaksi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtTanggal.setToolTipText("Format YYYY-MM-DD, contoh: 2026-11-28");
        txtJumlah.setToolTipText("Masukkan nominal angka bulat, contoh: 25000");

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Tanggal:"), gbc);
        gbc.gridx = 1;
        form.add(txtTanggal, gbc);

        gbc.gridx = 2;
        form.add(new JLabel("Jumlah (Rp):"), gbc);
        gbc.gridx = 3;
        form.add(txtJumlah, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1;
        form.add(cmbKategori, gbc);

        gbc.gridx = 2;
        form.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 3;
        form.add(txtCatatan, gbc);

        JButton btnSimpan = new JButton("Simpan Transaksi");
        btnSimpan.addActionListener(e -> simpanTransaksi());
        gbc.gridx = 4;
        gbc.gridy = 1;
        form.add(btnSimpan, gbc);

        wrapper.add(panelSaldo, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buatAksiPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            muatKategori();
            muatDataTransaksi();
        });

        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusTransaksiTerpilih());

        panel.add(btnRefresh);
        panel.add(btnHapus);
        return panel;
    }

    public void muatKategori() {
        cmbKategori.removeAllItems();
        try {
            for (Kategori k : kategoriService.ambilSemuaKategori()) {
                cmbKategori.addItem(new KategoriItem(k.getId(), k.getNama(), k.getJenis()));
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat kategori: " + ex.getMessage());
        }
    }

    public void muatDataTransaksi() {
        tableModel.setRowCount(0);
        try {
            Map<Integer, Kategori> petaKategori = new HashMap<>();
            for (Kategori k : kategoriService.ambilSemuaKategori()) {
                petaKategori.put(k.getId(), k);
            }

            daftarTransaksi = transaksiService.ambilSemuaTransaksi();
            for (Transaksi tr : daftarTransaksi) {
                Kategori k = petaKategori.get(tr.getKategoriId());
                String namaKategori = k != null ? k.getNama() : ("ID " + tr.getKategoriId());
                String jenis = k != null ? k.getJenis() : "-";

                tableModel.addRow(new Object[]{
                    tr.getId(),
                    tr.getTanggal(),
                    namaKategori,
                    jenis,
                    formatRupiah.format(tr.getJumlah()),
                    tr.getCatatan()
                });
            }

            BigDecimal saldo = transaksiService.hitungSaldo();
            lblSaldo.setText("Saldo: " + formatRupiah.format(saldo));
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat data transaksi: " + ex.getMessage());
        }
    }

    private void simpanTransaksi() {
        try {
            LocalDate tanggal = LocalDate.parse(txtTanggal.getText().trim());
            String teksJumlah = txtJumlah.getText().trim().replace(",", ".");
            BigDecimal jumlah = new BigDecimal(teksJumlah);

            KategoriItem terpilih = (KategoriItem) cmbKategori.getSelectedItem();
            if (terpilih == null) {
                tampilkanError("Pilih atau tambahkan kategori transaksi terlebih dahulu.");
                return;
            }

            String catatan = txtCatatan.getText();
            transaksiService.tambahTransaksi(tanggal, jumlah, terpilih.id, null, catatan);

            txtJumlah.setText("");
            txtCatatan.setText("");
            muatDataTransaksi();
            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan.");
        } catch (DateTimeParseException ex) {
            tampilkanError("Format tanggal harus YYYY-MM-DD (contoh: 2026-11-28).");
        } catch (NumberFormatException ex) {
            tampilkanError("Jumlah transaksi harus berupa angka yang valid.");
        } catch (IllegalArgumentException | SQLException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    private void hapusTransaksiTerpilih() {
        int row = tableTransaksi.getSelectedRow();
        if (row < 0 || row >= daftarTransaksi.size()) {
            tampilkanError("Pilih transaksi pada tabel terlebih dahulu.");
            return;
        }
        try {
            Transaksi terpilih = daftarTransaksi.get(row);
            transaksiService.hapusTransaksi(terpilih.getId());
            muatDataTransaksi();
        } catch (SQLException ex) {
            tampilkanError("Gagal menghapus transaksi: " + ex.getMessage());
        }
    }

    private void tampilkanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private static class KategoriItem {
        private final int id;
        private final String nama;
        private final String jenis;

        private KategoriItem(int id, String nama, String jenis) {
            this.id = id;
            this.nama = nama;
            this.jenis = jenis;
        }

        @Override
        public String toString() {
            return nama + " (" + jenis + ")";
        }
    }
}