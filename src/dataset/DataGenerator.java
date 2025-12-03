package dataset;
import dataset.ModelCalculator;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DataGenerator {

    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 40;
    private static final double MIN_DAYLIGHT = 8;
    private static final double MAX_DAYLIGHT = 15;
    private static double MIN_LOAD;
    private static double MAX_LOAD;

    // Rastgele sayı üreteci
    private static double rand(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public static void generateDataset() throws Exception {


        List<String> allRows = new ArrayList<>();

        double[] mm = ModelCalculator.findRealMinMax();
        MIN_LOAD = mm[0];
        MAX_LOAD = mm[1];

        for (int i = 0; i < 4000; i++) {

            double rawTemp = rand(0, 40);       // 0 ile 40 derece arası
            double rawDaylight = rand(8, 15);   // 8 ile 15 saat arası

            double rawConsumption = ModelCalculator.calculateElectricity(rawTemp, rawDaylight);

            
            double normTemp = (rawTemp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
            double normDaylight = (rawDaylight - MIN_DAYLIGHT) / (MAX_DAYLIGHT - MIN_DAYLIGHT);
            double normConsumption = (rawConsumption - MIN_LOAD) / (MAX_LOAD - MIN_LOAD);

            allRows.add(normTemp + "," + normDaylight + "," + normConsumption);
        }

        File directory = new File("datasets");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        FileWriter writer = new FileWriter("datasets/full_dataset.csv");
        
        for (String row : allRows) {
            writer.write(row + "\n");
        }

        writer.close();
        try {
            if (!directory.exists()) {
            	directory.mkdirs(); 
            }

            java.io.FileWriter configWriter = new java.io.FileWriter("datasets/config.txt");
            configWriter.write(String.format(java.util.Locale.US, "%.5f\n", MIN_LOAD));
            configWriter.write(String.format(java.util.Locale.US, "%.5f\n", MAX_LOAD));
            configWriter.close();
            
            System.out.println("✓ Config kaydedildi: Min=" + MIN_LOAD + " Max=" + MAX_LOAD);
            
        } catch (Exception e) {
            System.err.println("Konfigürasyon dosyası kaydedilemedi!");
            e.printStackTrace();
        }
        System.out.println("✓ full_dataset.csv başarıyla oluşturuldu.");
        System.out.println("✓ Toplam Satır: " + allRows.size());
    }
}