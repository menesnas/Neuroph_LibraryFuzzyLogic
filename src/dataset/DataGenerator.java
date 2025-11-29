package dataset;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DataGenerator {

    // Normalizasyon Sabitleri
    // Sıcaklık (0 - 40 arası varsayıldı)
    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 40;

    // Gün Işığı (8 - 15 saat arası varsayıldı)
    private static final double MIN_DAYLIGHT = 8;
    private static final double MAX_DAYLIGHT = 15;

    // Tüketim (1000 - 5000 kWh arası varsayıldı)
    private static final double MIN_LOAD = 1000;
    private static final double MAX_LOAD = 5000;

    // Rastgele sayı üreteci
    private static double rand(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public static void generateDataset() throws Exception {

        List<String> allRows = new ArrayList<>();

        System.out.println("Veri seti oluşturuluyor ve normalize ediliyor...");

        for (int i = 0; i < 4000; i++) {

            // Rastgele HAM veri üret (FCL uygun aralıkta)
            double rawTemp = rand(0, 40);       // 0 ile 40 derece arası
            double rawDaylight = rand(8, 15);   // 8 ile 15 saat arası

            // FCL modelinden HAM çıktıyı hesapla
            double rawConsumption = ModelCalculator.calculateElectricity(rawTemp, rawDaylight);

            // Verileri NORMALIZE ET (0 ile 1 arasına çek)
            // Formül: (Değer - Min) / (Max - Min)
            
            double normTemp = (rawTemp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
            double normDaylight = (rawDaylight - MIN_DAYLIGHT) / (MAX_DAYLIGHT - MIN_DAYLIGHT);
            double normConsumption = (rawConsumption - MIN_LOAD) / (MAX_LOAD - MIN_LOAD);

            // Listeye ekle (Virgülle ayrılmış: input1, input2, output)
            // Neuroph CSV formatı: Girdiler,Çıktılar
            allRows.add(normTemp + "," + normDaylight + "," + normConsumption);
        }

        // Tek bir dosyaya kaydet (full_dataset.csv)
        // ANNTrainer sınıfı bunu yükleyip %75-%25 bellek üzerinde bölecek.
        FileWriter writer = new FileWriter("datasets/full_dataset.csv");
        
        for (String row : allRows) {
            writer.write(row + "\n");
        }

        writer.close();

        System.out.println("✓ full_dataset.csv başarıyla oluşturuldu.");
        System.out.println("✓ Toplam Satır: " + allRows.size());
        System.out.println("✓ Veriler [0-1] aralığına normalize edildi.");
    }
}