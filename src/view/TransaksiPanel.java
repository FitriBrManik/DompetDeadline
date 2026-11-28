package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import model.Kategori;
import model.Transaksi;
import model.Tugas;
import service.CsvExportService;
import service.KategoriService;
import service.TransaksiService;
import service.TugasService;

public class TransaksiPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;
    private TugasService tugasService;
    private CsvExportService csvExportService;
    private Runnable onDataBerubah;

    private final JTextField txtTanggal = new JTextField(12);
    private final JTextField txtJumlah = new JTextField(14);
    private final JComboBox<KategoriItem> cmbKategori = new JComboBox<>();
    private final JComboBox<TugasItem> cmbTugas = new JComboBox<>();
    private final JTextField txtCatatan = new JTextField(22);

    private final JButton btnSimpan = new JButton("Simpan Transaksi");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnBatalEdit = new JButton("Batal");

    private final JLabel lblSaldo = new JLabel("Saldo: Rp 0");
    private final DefaultTableModel tableModel;
    private final JTable tableTransaksi;
    private List<Transaksi> daftarTransaksi = new ArrayList<>();
    private Integer idTransaksiSedangDiubah = null;

    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public TransaksiPanel(TransaksiService transaksiService, KategoriService kategoriService) {
        this(transaksiService, kategoriService, null);
    }

    public TransaksiPanel(
            TransaksiService transaksiService,
            KategoriService kategoriService,
            TugasService tugasService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;
        this.tugasService = tugasService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        txtTanggal.setText(LocalDate.now().toString());
        formatRupiah.setMaximumFractionDigits(0);

        add(buatPanelAtas(), BorderLayout.NORTH);

        String[] kolom = {"ID", "Tanggal", "Kategori", "Jenis", "Jumlah", "Tugas Terkait", "Catatan"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTransaksi = new JTable(tableModel);
        tableTransaksi.setAutoCreateRowSorter(true);
        add(new JScrollPane(tableTransaksi), BorderLayout.CENTER);

        add(buatAksiPanel(), BorderLayout.SOUTH);

        muatKategori();
        muatDaftarTugas();
        muatDataTransaksi();
    }

    public void setTugasService(TugasService tugasService) {
        this.tugasService = tugasService;
        muatDaftarTugas();
        muatDataTransaksi();
    }

    public void setCsvExportService(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    public void setOnDataBerubah(Runnable onDataBerubah) {
        this.onDataBerubah = onDataBerubah;
    }

    private void beriTahuDataBerubah() {
        if (onDataBerubah != null) {
            onDataBerubah.run();
        }
    }

    private JPanel buatPanelAtas() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));

        JPanel panelSaldo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSaldo.setBorder(BorderFactory.createTitledBorder("Ringkasan Dompet"));
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSaldo.add(lblSaldo);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Transaksi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtTanggal.setToolTipText("Format YYYY-MM-DD, contoh: 2026-11-28");
        txtJumlah.setToolTipText("Masukkan nominal angka bulat, contoh: 25000");
        btnBatalEdit.setVisible(false);

        JPanel panelKategori = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelKategori.add(cmbKategori);
        JButton btnTambahKategori = new JButton("+ Kategori");
        btnTambahKategori.addActionListener(e -> bukaDialogKategori());
        panelKategori.add(btnTambahKategori);

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
        form.add(panelKategori, gbc);

        gbc.gridx = 2;
        form.add(new JLabel("Tugas Terkait:"), gbc);
        gbc.gridx = 3;
        form.add(cmbTugas, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(txtCatatan, gbc);
        gbc.gridwidth = 1;

        JPanel panelTombolForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnSimpan.addActionListener(e -> simpanAtauPerbaruiTransaksi());
        btnBatalEdit.addActionListener(e -> resetForm());
        panelTombolForm.add(btnSimpan);
        panelTombolForm.add(btnBatalEdit);

        gbc.gridx = 3;
        gbc.gridy = 2;
        form.add(panelTombolForm, gbc);

        wrapper.add(panelSaldo, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buatAksiPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnEksporCsv = new JButton("Ekspor CSV");
        btnEksporCsv.addActionListener(e -> eksporCsv());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            muatKategori();
            muatDaftarTugas();
            muatDataTransaksi();
            beriTahuDataBerubah();
        });

        btnUbah.addActionListener(e -> mulaiUbahTransaksi());

        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusTransaksiTerpilih());

        panel.add(btnEksporCsv);
        panel.add(btnRefresh);
        panel.add(btnUbah);
        panel.add(btnHapus);
        return panel;
    }

    private void bukaDialogKategori() {
        Window induk = SwingUtilities.getWindowAncestor(this);
        KategoriDialog dialog = new KategoriDialog(induk, kategoriService);
        dialog.setVisible(true);
        if (dialog.isBerhasilTambah()) {
            muatKategori();
            beriTahuDataBerubah();
        }
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

    public void muatDaftarTugas() {
        cmbTugas.removeAllItems();
        cmbTugas.addItem(new TugasItem(null, "- Tidak terkait tugas -"));
        if (tugasService == null) {
            return;
        }
        try {
            for (Tugas t : tugasService.ambilSemuaTugas()) {
                cmbTugas.addItem(new TugasItem(t.getId(), t.getJudul()));
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat daftar tugas: " + ex.getMessage());
        }
    }

    public void muatDataTransaksi() {
        tableModel.setRowCount(0);
        try {
            Map<Integer, Kategori> petaKategori = new HashMap<>();
            for (Kategori k : kategoriService.ambilSemuaKategori()) {
                petaKategori.put(k.getId(), k);
            }

            Map<Integer, String> petaJudulTugas = new HashMap<>();
            if (tugasService != null) {
                for (Tugas t : tugasService.ambilSemuaTugas()) {
                    petaJudulTugas.put(t.getId(), t.getJudul());
                }
            }

            daftarTransaksi = transaksiService.ambilSemuaTransaksi();
            for (Transaksi tr : daftarTransaksi) {
                Kategori k = petaKategori.get(tr.getKategoriId());
                String namaKategori = k != null ? k.getNama() : ("ID " + tr.getKategoriId());
                String jenis = k != null ? k.getJenis() : "-";

                String labelTugas = "-";
                if (tr.getTugasId() != null) {
                    labelTugas = petaJudulTugas.getOrDefault(tr.getTugasId(), "Tugas #" + tr.getTugasId());
                }

                tableModel.addRow(new Object[]{
                    tr.getId(),
                    tr.getTanggal(),
                    namaKategori,
                    jenis,
                    formatRupiah.format(tr.getJumlah()),
                    labelTugas,
                    tr.getCatatan()
                });
            }

            BigDecimal saldo = transaksiService.hitungSaldo();
            lblSaldo.setText("Saldo: " + formatRupiah.format(saldo));
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat data transaksi: " + ex.getMessage());
        }
    }

    private void simpanAtauPerbaruiTransaksi() {
        try {
            LocalDate tanggal = LocalDate.parse(txtTanggal.getText().trim());
            String teksJumlah = txtJumlah.getText().trim().replace(",", ".");
            BigDecimal jumlah = new BigDecimal(teksJumlah);

            KategoriItem kategoriTerpilih = (KategoriItem) cmbKategori.getSelectedItem();
            if (kategoriTerpilih == null) {
                tampilkanError("Pilih atau tambahkan kategori transaksi terlebih dahulu.");
                return;
            }

            TugasItem tugasTerpilih = (TugasItem) cmbTugas.getSelectedItem();
            Integer tugasId = (tugasTerpilih != null) ? tugasTerpilih.id : null;
            String catatan = txtCatatan.getText();

            if (idTransaksiSedangDiubah == null) {
                transaksiService.tambahTransaksi(tanggal, jumlah, kategoriTerpilih.id, tugasId, catatan);
                resetForm();
                muatDataTransaksi();
                beriTahuDataBerubah();
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan.");
            } else {
                boolean diperbarui = transaksiService.ubahTransaksi(
                        idTransaksiSedangDiubah,
                        tanggal,
                        jumlah,
                        kategoriTerpilih.id,
                        tugasId,
                        catatan
                );
                if (!diperbarui) {
                    tampilkanError("Data transaksi tidak ditemukan untuk diubah.");
                    return;
                }
                resetForm();
                muatDataTransaksi();
                beriTahuDataBerubah();
                JOptionPane.showMessageDialog(this, "Transaksi berhasil diperbarui.");
            }
        } catch (DateTimeParseException ex) {
            tampilkanError("Format tanggal harus YYYY-MM-DD (contoh: 2026-11-28).");
        } catch (NumberFormatException ex) {
            tampilkanError("Jumlah transaksi harus berupa angka yang valid.");
        } catch (IllegalArgumentException | SQLException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    private void mulaiUbahTransaksi() {
        Transaksi terpilih = ambilTransaksiTerpilih();
        if (terpilih == null) {
            tampilkanError("Pilih transaksi pada tabel terlebih dahulu.");
            return;
        }
        idTransaksiSedangDiubah = terpilih.getId();
        txtTanggal.setText(terpilih.getTanggal() != null ? terpilih.getTanggal().toString() : LocalDate.now().toString());
        txtJumlah.setText(terpilih.getJumlah() != null ? terpilih.getJumlah().toPlainString() : "");
        txtCatatan.setText(terpilih.getCatatan() != null ? terpilih.getCatatan() : "");

        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
            KategoriItem item = cmbKategori.getItemAt(i);
            if (item.id == terpilih.getKategoriId()) {
                cmbKategori.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < cmbTugas.getItemCount(); i++) {
            TugasItem item = cmbTugas.getItemAt(i);
            if ((terpilih.getTugasId() == null && item.id == null)
                    || (terpilih.getTugasId() != null && terpilih.getTugasId().equals(item.id))) {
                cmbTugas.setSelectedIndex(i);
                break;
            }
        }

        btnSimpan.setText("Simpan Perubahan");
        btnBatalEdit.setVisible(true);
    }

    private void resetForm() {
        idTransaksiSedangDiubah = null;
        txtJumlah.setText("");
        txtCatatan.setText("");
        if (cmbTugas.getItemCount() > 0) {
            cmbTugas.setSelectedIndex(0);
        }
        btnSimpan.setText("Simpan Transaksi");
        btnBatalEdit.setVisible(false);
    }

    private void hapusTransaksiTerpilih() {
        Transaksi terpilih = ambilTransaksiTerpilih();
        if (terpilih == null) {
            tampilkanError("Pilih transaksi pada tabel terlebih dahulu.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus transaksi tanggal " + terpilih.getTanggal() + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            transaksiService.hapusTransaksi(terpilih.getId());
            if (idTransaksiSedangDiubah != null && idTransaksiSedangDiubah == terpilih.getId()) {
                resetForm();
            }
            muatDataTransaksi();
            beriTahuDataBerubah();
        } catch (SQLException ex) {
            tampilkanError("Gagal menghapus transaksi: " + ex.getMessage());
        }
    }

    private Transaksi ambilTransaksiTerpilih() {
        int viewRow = tableTransaksi.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tableTransaksi.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= daftarTransaksi.size()) {
            return null;
        }
        return daftarTransaksi.get(modelRow);
    }

    private void eksporCsv() {
        if (csvExportService == null) {
            tampilkanError("Layanan ekspor CSV belum terpasang.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Data Transaksi CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setSelectedFile(new File("transaksi-dompet-deadline.csv"));

        int pilihan = chooser.showSaveDialog(this);
        if (pilihan == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            try {
                csvExportService.eksporTransaksiKeCsv(file);
                JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke: " + file.getAbsolutePath());
            } catch (SQLException | IOException ex) {
                tampilkanError("Gagal mengekspor CSV: " + ex.getMessage());
            }
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

    private static class TugasItem {
        private final Integer id;
        private final String judul;

        private TugasItem(Integer id, String judul) {
            this.id = id;
            this.judul = judul;
        }

        @Override
        public String toString() {
            return judul;
        }
    }
}