package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import service.KategoriService;

public class KategoriDialog extends JDialog {

    private final KategoriService kategoriService;
    private final JTextField txtNama = new JTextField(18);
    private final JComboBox<String> cmbJenis = new JComboBox<>(new String[]{"pengeluaran", "pemasukan"});
    private boolean berhasilTambah = false;

    public KategoriDialog(Window owner, KategoriService kategoriService) {
        super(owner, "Tambah Kategori", ModalityType.APPLICATION_MODAL);
        this.kategoriService = kategoriService;

        setLayout(new BorderLayout(10, 10));
        setSize(360, 180);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Nama Kategori:"), gbc);
        gbc.gridx = 1;
        form.add(txtNama, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Jenis:"), gbc);
        gbc.gridx = 1;
        form.add(cmbJenis, gbc);

        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");

        btnSimpan.addActionListener(e -> simpanKategori());
        btnBatal.addActionListener(e -> dispose());

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTombol.add(btnBatal);
        panelTombol.add(btnSimpan);

        getRootPane().setDefaultButton(btnSimpan);

        add(form, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);
    }

    private void simpanKategori() {
        String nama = txtNama.getText().trim();
        String jenis = (String) cmbJenis.getSelectedItem();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nama kategori tidak boleh kosong. Isi nama kategori lalu klik Simpan.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );
            txtNama.requestFocusInWindow();
            return;
        }

        try {
            kategoriService.tambahKategori(nama, jenis);
            berhasilTambah = true;
            JOptionPane.showMessageDialog(this, "Kategori \"" + nama + "\" berhasil ditambahkan.");
            dispose();
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menambah kategori: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isBerhasilTambah() {
        return berhasilTambah;
    }
}