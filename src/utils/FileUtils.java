package utils;

import java.io.BufferedReader;
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

                // ✔ SÜRÜM BAĞIMSIZ YÖNTEM
                ds.getRows().add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
}
