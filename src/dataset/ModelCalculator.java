package dataset;

import net.sourceforge.jFuzzyLogic.FIS;

public class ModelCalculator {

    // FCL model dosyasının yolu
    private static final String FCL_PATH = "src/dataset/model.fcl";

    public static double calculateElectricity(double temperature, double dayLight) {

        // FCL dosyasını yükle
        FIS fis = FIS.load(FCL_PATH, true);
        if (fis == null) {
            System.err.println("FCL dosyası yüklenemedi: " + FCL_PATH);
            return 0;
        }

        // Girdileri set et
        fis.setVariable("temperature", temperature);
        fis.setVariable("day_light_duration", dayLight);

        // Çıkarım (inference)
        fis.evaluate();

        // Çıktıyı al
        double output = fis.getVariable("electricity_consumption").getValue();

        return output;
    }
}