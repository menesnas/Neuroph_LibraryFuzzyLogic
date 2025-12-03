package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GraphPlotter {

    // CSV dosyasının okunacağı yer (datasets klasörü içinde)
    private static final String CSV_FILE = "datasets/epoch_results.csv";
    // Grafiğin kaydedileceği dosya adı
    private static final String OUTPUT_IMAGE = "SonucGrafigi.png";
    
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int PADDING = 80; // Yazılar için boşluğu artırdık
    private static final int LABEL_PADDING = 30;

    // 10 Farklı Ağ için 10 Farklı Renk
    private static final Color[] NETWORK_COLORS = {
        Color.RED, Color.BLUE, new Color(0, 150, 0), Color.ORANGE, Color.MAGENTA,
        Color.CYAN, Color.PINK, Color.DARK_GRAY, new Color(128, 0, 128), new Color(0, 128, 128)
    };

    public static void drawAndSaveGraph() {
        List<String[]> data = readCSV();
        if (data.isEmpty()) {
            System.out.println("Grafik çizilecek veri bulunamadı! Lütfen epoch_results.csv dosyasını kontrol edin.");
            return;
        }

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Arka Plan
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Verileri Hazırla
        int numEpochs = data.size();
        int numNetworks = (data.get(0).length - 1) / 2;

        // Maksimum MSE değerini bul (Y ekseni tavanı için)
        double maxMSE = 0;
        for (String[] row : data) {
            for (int i = 1; i <= numNetworks; i++) {
                try {
                    double val = Double.parseDouble(row[i * 2]); // Test sütunları
                    if (val > maxMSE) maxMSE = val;
                } catch (NumberFormatException e) { continue; }
            }
        }
        // Grafiğin tepesine yapışmaması için max değere biraz pay ekle
        maxMSE = maxMSE * 1.1; 

        // 2. Izgara Çizgileri ve Eksen Değerleri (Grid & Ticks)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        // Y Ekseni (MSE) Çizgileri (10 Adet)
        for (int i = 0; i <= 10; i++) {
            int y = HEIGHT - PADDING - LABEL_PADDING - (i * (HEIGHT - 2 * PADDING - 2 * LABEL_PADDING) / 10);
            double val = maxMSE * i / 10.0;
            String yLabel = String.format("%.4f", val);
            
            // Izgara çizgisi (Gri)
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(PADDING + LABEL_PADDING, y, WIDTH - PADDING, y);

            // Sayı Değeri (Siyah)
            g2.setColor(Color.BLACK);
            int labelWidth = fm.stringWidth(yLabel);
            g2.drawString(yLabel, PADDING + LABEL_PADDING - labelWidth - 5, y + (fm.getAscent() / 2) - 1);
        }

        // X Ekseni (Epoch) Çizgileri (10 veya Epoch sayısına göre)
        int xStep = Math.max(1, numEpochs / 10); 
        for (int i = 0; i < numEpochs; i += xStep) {
            int x = (int) ((i * 1.0 / (numEpochs - 1)) * (WIDTH - 2 * PADDING - 2 * LABEL_PADDING) + PADDING + LABEL_PADDING);
            
            // Izgara çizgisi (Dikey)
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(x, PADDING, x, HEIGHT - PADDING - LABEL_PADDING);

            // Sayı Değeri
            g2.setColor(Color.BLACK);
            String xLabel = String.valueOf(i + 1);
            int labelWidth = fm.stringWidth(xLabel);
            g2.drawString(xLabel, x - (labelWidth / 2), HEIGHT - PADDING - LABEL_PADDING + fm.getHeight() + 3);
        }

        // 3. Eksen Çizgileri (Ana Siyah Çizgiler)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(PADDING + LABEL_PADDING, HEIGHT - PADDING - LABEL_PADDING, PADDING + LABEL_PADDING, PADDING); // Y Ekseni
        g2.drawLine(PADDING + LABEL_PADDING, HEIGHT - PADDING - LABEL_PADDING, WIDTH - PADDING, HEIGHT - PADDING - LABEL_PADDING); // X Ekseni

        // 4. Eksen Başlıkları
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Epoch Sayısı", WIDTH / 2, HEIGHT - PADDING + 10);
        
        // Y Eksen başlığını döndürerek yazmak zor olduğu için üste yazıyoruz
        g2.drawString("MSE Hata Oranı", PADDING - 20, PADDING - 10);

        // Başlık
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String title = "10 Farklı Ağ Topolojisi - Test Hatası Karşılaştırması";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, PADDING / 2);

        // 5. Grafikleri Çiz
        g2.setStroke(new BasicStroke(2f));
        
        for (int netIndex = 0; netIndex < numNetworks; netIndex++) {
            g2.setColor(NETWORK_COLORS[netIndex % NETWORK_COLORS.length]);
            
            // Test sütunu indexi: (netIndex + 1) * 2
            // Örn: Net1 -> Index 2, Net2 -> Index 4...
            int colIndex = (netIndex + 1) * 2;

            int prevX = -1;
            int prevY = -1;

            for (int i = 0; i < numEpochs; i++) {
                double val = Double.parseDouble(data.get(i)[colIndex]);
                
                int x = (int) ((i * 1.0 / (numEpochs - 1)) * (WIDTH - 2 * PADDING - 2 * LABEL_PADDING) + PADDING + LABEL_PADDING);
                int y = (int) ((1 - (val / maxMSE)) * (HEIGHT - 2 * PADDING - 2 * LABEL_PADDING) + PADDING);

                if (i > 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }

            // 6. Lejant (Sağ Üst Köşe)
            int legendX = WIDTH - PADDING - 100;
            int legendY = PADDING + (netIndex * 20);
            
            // Renk kutusu
            g2.fillRect(legendX, legendY, 10, 10);
            // Yazı
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Ağ " + (netIndex + 1), legendX + 15, legendY + 10);
        }

        g2.dispose();

        // Resmi Kaydet
        try {
            File outputFile = new File(OUTPUT_IMAGE);
            ImageIO.write(image, "png", outputFile);
            System.out.println("✓ Grafik oluşturuldu: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String[]> readCSV() {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Başlığı atla
                records.add(line.split(","));
            }
        } catch (Exception e) {
            System.err.println("CSV okuma hatası: " + e.getMessage());
        }
        return records;
    }
}