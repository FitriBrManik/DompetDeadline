package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
import model.Tugas;
import service.TugasService;

public class TugasPanel extends JPanel {

    private final TugasService tugasService;

    private final JTextField txtJudul = new JTextField(20);
    private final JTextField txtMataKuliah = new JTextField(20);
    private final JTextField txtTenggat = new JTextField(12);
    private final JComboBox<String> cmbPrioritas = new JComboBox<>(new String[]{"rendah", "sedang", "tinggi"});

    private final DefaultTableModel tableModel;
    private final JTable tableTugas;
    private List<Tugas> daftarTugas = new ArrayList<>();

    public TugasPanel(TugasService tugasService) {
        this.tugasService = tugasService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buatFormPanel(), BorderLayout.NORTH);

        String[] kolom = {"ID", "Judul", "Mata Kuliah", "Tenggat", "Prioritas", "Status"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTugas = new JTable(tableModel);
        add(new JScrollPane(tableTugas), BorderLayout.CENTER);

        add(buatAksiPanel(), BorderLayout.SOUTH);

        muatDataTugas();
    }

    private JPanel buatFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tambah Tugas / Deadline"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtTenggat.setToolTipText("Format YYYY-MM-DD, contoh: 2026-12-05");
        cmbPrioritas.setSelectedItem("sedang");

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

        JButton btnSimpan = new JButton("Simpan Tugas");
        btnSimpan.addActionListener(e -> simpanTugas());
        gbc.gridx = 4;
        gbc.gridy = 1;
        panel.add(btnSimpan, gbc);

        return panel;
    }

    private JPanel buatAksiPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> muatDataTugas());

        JButton btnSelesai = new JButton("Tandai Selesai");
        btnSelesai.addActionListener(e -> tandaiTugasSelesai());

        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusTugasTerpilih());

        panel.add(btnRefresh);
        panel.add(btnSelesai);
        panel.add(btnHapus);
        return panel;
    }

    public void muatDataTugas() {
        tableModel.setRowCount(0);
        try {
            daftarTugas = tugasService.ambilSemuaTugas();
            for (Tugas t : daftarTugas) {
                tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getJudul(),
                    t.getMataKuliah(),
                    t.getTenggat(),
                    t.getPrioritas(),
                    t.isSelesai() ? "Selesai" : "Belum Selesai"
                });
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat daftar tugas: " + ex.getMessage());
        }
    }

    private void simpanTugas() {
        try {
            String judul = txtJudul.getText();
            String mataKuliah = txtMataKuliah.getText();
            LocalDate tenggat = LocalDate.parse(txtTenggat.getText().trim());
            String prioritas = (String) cmbPrioritas.getSelectedItem();

            tugasService.tambahTugas(judul, mataKuliah, tenggat, prioritas);
            txtJudul.setText("");
            txtMataKuliah.setText("");
            txtTenggat.setText("");
            cmbPrioritas.setSelectedItem("sedang");
            muatDataTugas();
            JOptionPane.showMessageDialog(this, "Tugas berhasil disimpan.");
        } catch (DateTimeParseException ex) {
            tampilkanError("Format tanggal tenggat harus YYYY-MM-DD (contoh: 2026-12-05).");
        } catch (IllegalArgumentException | SQLException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    private void tandaiTugasSelesai() {
        int row = tableTugas.getSelectedRow();
        if (row < 0 || row >= daftarTugas.size()) {
            tampilkanError("Pilih tugas pada tabel terlebih dahulu.");
            return;
        }
        try {
            Tugas terpilih = daftarTugas.get(row);
            tugasService.tandaiSelesai(terpilih.getId());
            muatDataTugas();
        } catch (SQLException ex) {
            tampilkanError("Gagal memperbarui status tugas: " + ex.getMessage());
        }
    }

    private void hapusTugasTerpilih() {
        int row = tableTugas.getSelectedRow();
        if (row < 0 || row >= daftarTugas.size()) {
            tampilkanError("Pilih tugas pada tabel terlebih dahulu.");
            return;
        }
        try {
            Tugas terpilih = daftarTugas.get(row);
            tugasService.hapusTugas(terpilih.getId());
            muatDataTugas();
        } catch (SQLException ex) {
            tampilkanError("Gagal menghapus tugas: " + ex.getMessage());
        }
    }

    private void tampilkanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}