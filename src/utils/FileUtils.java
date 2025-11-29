package utils;

import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileReader;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

public class FileUtils {

    public static DataSet loadCSV(String path, int inputs, int outputs) {

        DataSet ds = new DataSet(inputs, outputs);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");

                double in1 = Double.parseDouble(p[0]);
                double in2 = Double.parseDouble(p[1]);
                double out = Double.parseDouble(p[2]);

                DataSetRow row = new DataSetRow(
                    new double[]{in1, in2},
                    new double[]{out}
                );

                ds.getRows().add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
    public static void exportEpochErrors(List<double[]> trainErrors, List<double[]> testErrors) {

        try (java.io.FileWriter fw = new java.io.FileWriter("epoch_results.csv")) {

            int networks = trainErrors.size();
            int epochs = trainErrors.get(0).length;

            fw.write("Epoch");

            for (int i = 1; i <= networks; i++) {
                fw.write(",Train_Net" + i + ",Test_Net" + i);
            }
            fw.write("\n");

            // Her epoch için tüm ağların hataları
            for (int e = 0; e < epochs; e++) {

                fw.write(String.valueOf(e + 1));  // epoch numarası

                for (int n = 0; n < networks; n++) {
                    fw.write("," + trainErrors.get(n)[e] + "," + testErrors.get(n)[e]);
                }
                fw.write("\n");
            }

            System.out.println("✓ epoch_results.csv başarıyla oluşturuldu.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
