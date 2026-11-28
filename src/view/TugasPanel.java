package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Tugas;
import service.TransaksiService;
import service.TugasService;

public class TugasPanel extends JPanel {

    private final TugasService tugasService;
    private final TransaksiService transaksiService;
    private Runnable onDataBerubah;

    private final JTextField txtJudul = new JTextField(20);
    private final JTextField txtMataKuliah = new JTextField(20);
    private final JTextField txtTenggat = new JTextField(12);
    private final JComboBox<String> cmbPrioritas = new JComboBox<>(new String[]{"rendah", "sedang", "tinggi"});
    private final JCheckBox chkSelesai = new JCheckBox("Sudah selesai");

    private final JButton btnSimpan = new JButton("Simpan Tugas");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnBatalEdit = new JButton("Batal");

    private final DefaultTableModel tableModel;
    private final JTable tableTugas;
    private List<Tugas> daftarTugas = new ArrayList<>();
    private Integer idTugasSedangDiubah = null;

    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public TugasPanel(TugasService tugasService) {
        this(tugasService, null);
    }

    public TugasPanel(TugasService tugasService, TransaksiService transaksiService) {
        this.tugasService = tugasService;
        this.transaksiService = transaksiService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        formatRupiah.setMaximumFractionDigits(0);

        add(buatFormPanel(), BorderLayout.NORTH);

        String[] kolom = {"ID", "Judul", "Mata Kuliah", "Tenggat", "Prioritas", "Status", "Total Biaya"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTugas = new JTable(tableModel);
        tableTugas.setAutoCreateRowSorter(true);
        add(new JScrollPane(tableTugas), BorderLayout.CENTER);

        add(buatAksiPanel(), BorderLayout.SOUTH);

        muatDataTugas();
    }

    public void setOnDataBerubah(Runnable onDataBerubah) {
        this.onDataBerubah = onDataBerubah;
    }

    private void beriTahuDataBerubah() {
        if (onDataBerubah != null) {
            onDataBerubah.run();
        }
    }

    private JPanel buatFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Form Tugas / Deadline"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtTenggat.setToolTipText("Format YYYY-MM-DD, contoh: 2026-12-05");
        cmbPrioritas.setSelectedItem("sedang");
        chkSelesai.setVisible(false);
        btnBatalEdit.setVisible(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Judul:"), gbc);
        gbc.gridx = 1;
        panel.add(txtJudul, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Tenggat (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        panel.add(txtTenggat, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Mata Kuliah:"), gbc);
        gbc.gridx = 1;
        panel.add(txtMataKuliah, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Prioritas:"), gbc);
        gbc.gridx = 3;
        panel.add(cmbPrioritas, gbc);

        JPanel panelTombolForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnSimpan.addActionListener(e -> simpanAtauPerbaruiTugas());
        btnBatalEdit.addActionListener(e -> resetForm());
        panelTombolForm.add(chkSelesai);
        panelTombolForm.add(btnSimpan);
        panelTombolForm.add(btnBatalEdit);

        gbc.gridx = 4;
        gbc.gridy = 1;
        panel.add(panelTombolForm, gbc);

        return panel;
    }

    private JPanel buatAksiPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            muatDataTugas();
            beriTahuDataBerubah();
        });

        btnUbah.addActionListener(e -> mulaiUbahTugas());

        JButton btnSelesai = new JButton("Tandai Selesai");
        btnSelesai.addActionListener(e -> tandaiTugasSelesai());

        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusTugasTerpilih());

        panel.add(btnRefresh);
        panel.add(btnUbah);
        panel.add(btnSelesai);
        panel.add(btnHapus);
        return panel;
    }

    public void muatDataTugas() {
        tableModel.setRowCount(0);
        try {
            daftarTugas = tugasService.ambilSemuaTugas();
            Map<Integer, BigDecimal> biayaPerTugas = (transaksiService != null)
                    ? transaksiService.hitungTotalBiayaPerTugas()
                    : new HashMap<>();

            for (Tugas t : daftarTugas) {
                BigDecimal totalBiaya = biayaPerTugas.getOrDefault(t.getId(), BigDecimal.ZERO);
                tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getJudul(),
                    t.getMataKuliah(),
                    t.getTenggat(),
                    t.getPrioritas(),
                    t.isSelesai() ? "Selesai" : "Belum Selesai",
                    formatRupiah.format(totalBiaya)
                });
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat daftar tugas: " + ex.getMessage());
        }
    }

    private void simpanAtauPerbaruiTugas() {
        try {
            String judul = txtJudul.getText();
            String mataKuliah = txtMataKuliah.getText();
            LocalDate tenggat = LocalDate.parse(txtTenggat.getText().trim());
            String prioritas = (String) cmbPrioritas.getSelectedItem();

            if (idTugasSedangDiubah == null) {
                tugasService.tambahTugas(judul, mataKuliah, tenggat, prioritas);
                resetForm();
                muatDataTugas();
                beriTahuDataBerubah();
                JOptionPane.showMessageDialog(this, "Tugas berhasil disimpan.");
            } else {
                boolean diperbarui = tugasService.ubahTugas(
                        idTugasSedangDiubah,
                        judul,
                        mataKuliah,
                        tenggat,
                        prioritas,
                        chkSelesai.isSelected()
                );
                if (!diperbarui) {
                    tampilkanError("Data tugas tidak ditemukan untuk diubah.");
                    return;
                }
                resetForm();
                muatDataTugas();
                beriTahuDataBerubah();
                JOptionPane.showMessageDialog(this, "Tugas berhasil diperbarui.");
            }
        } catch (DateTimeParseException ex) {
            tampilkanError("Format tanggal tenggat harus YYYY-MM-DD (contoh: 2026-12-05).");
        } catch (IllegalArgumentException | SQLException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    private void mulaiUbahTugas() {
        Tugas terpilih = ambilTugasTerpilih();
        if (terpilih == null) {
            tampilkanError("Pilih tugas pada tabel terlebih dahulu.");
            return;
        }
        idTugasSedangDiubah = terpilih.getId();
        txtJudul.setText(terpilih.getJudul());
        txtMataKuliah.setText(terpilih.getMataKuliah());
        txtTenggat.setText(terpilih.getTenggat() != null ? terpilih.getTenggat().toString() : "");
        cmbPrioritas.setSelectedItem(terpilih.getPrioritas());
        chkSelesai.setSelected(terpilih.isSelesai());
        chkSelesai.setVisible(true);
        btnSimpan.setText("Simpan Perubahan");
        btnBatalEdit.setVisible(true);
    }

    private void resetForm() {
        idTugasSedangDiubah = null;
        txtJudul.setText("");
        txtMataKuliah.setText("");
        txtTenggat.setText("");
        cmbPrioritas.setSelectedItem("sedang");
        chkSelesai.setSelected(false);
        chkSelesai.setVisible(false);
        btnSimpan.setText("Simpan Tugas");
        btnBatalEdit.setVisible(false);
    }

    private void tandaiTugasSelesai() {
        Tugas terpilih = ambilTugasTerpilih();
        if (terpilih == null) {
            tampilkanError("Pilih tugas pada tabel terlebih dahulu.");
            return;
        }
        try {
            tugasService.tandaiSelesai(terpilih.getId());
            muatDataTugas();
            beriTahuDataBerubah();
        } catch (SQLException ex) {
            tampilkanError("Gagal memperbarui status tugas: " + ex.getMessage());
        }
    }

    private void hapusTugasTerpilih() {
        Tugas terpilih = ambilTugasTerpilih();
        if (terpilih == null) {
            tampilkanError("Pilih tugas pada tabel terlebih dahulu.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus tugas \"" + terpilih.getJudul() + "\"?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            tugasService.hapusTugas(terpilih.getId());
            if (idTugasSedangDiubah != null && idTugasSedangDiubah == terpilih.getId()) {
                resetForm();
            }
            muatDataTugas();
            beriTahuDataBerubah();
        } catch (SQLException ex) {
            tampilkanError("Gagal menghapus tugas: " + ex.getMessage());
        }
    }

    private Tugas ambilTugasTerpilih() {
        int viewRow = tableTugas.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tableTugas.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= daftarTugas.size()) {
            return null;
        }
        return daftarTugas.get(modelRow);
    }

    private void tampilkanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}