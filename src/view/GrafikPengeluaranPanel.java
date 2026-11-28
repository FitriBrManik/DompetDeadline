package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JPanel;

public class GrafikPengeluaranPanel extends JPanel {

    private Map<String, BigDecimal> dataKategori = new LinkedHashMap<>();
    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public GrafikPengeluaranPanel() {
        setPreferredSize(new Dimension(600, 260));
        setMinimumSize(new Dimension(360, 220));
        setBackground(Color.WHITE);
        formatRupiah.setMaximumFractionDigits(0);
    }

    public void setDataKategori(Map<String, BigDecimal> dataKategori) {
        this.dataKategori = dataKategori != null ? new LinkedHashMap<>(dataKategori) : new LinkedHashMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int marginKiri = 85;
        int marginKanan = 24;
        int marginAtas = 32;
        int marginBawah = 52;

        int plotWidth = Math.max(80, width - marginKiri - marginKanan);
        int plotHeight = Math.max(60, height - marginAtas - marginBawah);
        int baseY = marginAtas + plotHeight;

        g2.setColor(new Color(70, 70, 70));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(marginKiri, marginAtas, marginKiri, baseY);
        g2.drawLine(marginKiri, baseY, marginKiri + plotWidth, baseY);

        if (dataKategori.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            g2.setColor(Color.GRAY);
            String pesan = "Belum ada data pengeluaran pada bulan yang dipilih.";
            FontMetrics fm = g2.getFontMetrics();
            int xTengah = marginKiri + (plotWidth - fm.stringWidth(pesan)) / 2;
            int yTengah = marginAtas + plotHeight / 2;
            g2.drawString(pesan, Math.max(marginKiri + 8, xTengah), yTengah);
            g2.dispose();
            return;
        }

        BigDecimal maxNilai = BigDecimal.ZERO;
        for (BigDecimal nilai : dataKategori.values()) {
            if (nilai.compareTo(maxNilai) > 0) {
                maxNilai = nilai;
            }
        }
        if (maxNilai.compareTo(BigDecimal.ZERO) <= 0) {
            maxNilai = BigDecimal.ONE;
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fmY = g2.getFontMetrics();
        int langkahGrid = 3;
        for (int i = 0; i <= langkahGrid; i++) {
            double rasio = (double) i / langkahGrid;
            int yPos = baseY - (int) Math.round(rasio * plotHeight);
            BigDecimal nilaiGrid = maxNilai.multiply(BigDecimal.valueOf(rasio));

            if (i > 0) {
                g2.setColor(new Color(230, 232, 236));
                g2.drawLine(marginKiri + 1, yPos, marginKiri + plotWidth, yPos);
            }

            String labelY = formatRupiah.format(nilaiGrid);
            int xLabelY = Math.max(4, marginKiri - fmY.stringWidth(labelY) - 8);
            g2.setColor(new Color(80, 80, 80));
            g2.drawString(labelY, xLabelY, yPos + 4);
        }

        var entries = new ArrayList<>(dataKategori.entrySet());
        int jumlahBatang = entries.size();
        double slotWidth = (double) plotWidth / jumlahBatang;
        int barWidth = (int) Math.max(18, Math.min(72, slotWidth * 0.52));

        Color[] warnaBatang = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(239, 68, 68),
            new Color(139, 92, 246),
            new Color(20, 184, 166)
        };

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < jumlahBatang; i++) {
            var entry = entries.get(i);
            String kategori = entry.getKey();
            BigDecimal nilai = entry.getValue();

            double rasio = nilai.doubleValue() / maxNilai.doubleValue();
            int barHeight = Math.max(2, (int) Math.round(rasio * (plotHeight - 16)));
            int slotStartX = marginKiri + (int) Math.round(i * slotWidth);
            int barX = slotStartX + ((int) slotWidth - barWidth) / 2;
            int barY = baseY - barHeight;

            g2.setColor(warnaBatang[i % warnaBatang.length]);
            g2.fillRect(barX, barY, barWidth, barHeight);

            g2.setColor(warnaBatang[i % warnaBatang.length].darker());
            g2.drawRect(barX, barY, barWidth, barHeight);

            String teksNilai = formatRupiah.format(nilai);
            int xNilai = barX + (barWidth - fm.stringWidth(teksNilai)) / 2;
            int yNilai = Math.max(marginAtas + 10, barY - 4);
            g2.setColor(new Color(35, 35, 35));
            g2.drawString(teksNilai, Math.max(marginKiri + 2, xNilai), yNilai);

            String labelKat = potongTeks(kategori, fm, (int) slotWidth - 6);
            int xKat = slotStartX + ((int) slotWidth - fm.stringWidth(labelKat)) / 2;
            int yKat = baseY + 18;
            g2.setColor(new Color(45, 45, 45));
            g2.drawString(labelKat, Math.max(marginKiri, xKat), yKat);
        }

        g2.dispose();
    }

    private String potongTeks(String teks, FontMetrics fm, int maxLebar) {
        if (maxLebar <= 10 || fm.stringWidth(teks) <= maxLebar) {
            return teks;
        }
        String elipsis = "...";
        int lebarElipsis = fm.stringWidth(elipsis);
        StringBuilder sb = new StringBuilder();
        for (char c : teks.toCharArray()) {
            if (fm.stringWidth(sb.toString() + c) + lebarElipsis > maxLebar) {
                break;
            }
            sb.append(c);
        }
        return sb.append(elipsis).toString();
    }
}