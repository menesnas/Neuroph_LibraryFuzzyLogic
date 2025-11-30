package dataset;

import net.sourceforge.jFuzzyLogic.FIS;

public class ModelCalculator {

	public static double[] findRealMinMax() {
	    double realMin = Double.MAX_VALUE;
	    double realMax = -Double.MAX_VALUE;

	    // 20.000 rastgele FCL hesaplaması (istatistiksel olarak yeterli)
	    for (int i = 0; i < 20000; i++) {
	        double t = Math.random() * 40;      // 0–40 derece
	        double d = 8 + Math.random() * 7;   // 8–15 saat

	        double out = calculateElectricity(t, d);

	        if (out < realMin) realMin = out;
	        if (out > realMax) realMax = out;
	    }

	    System.out.println("Gerçek FCL Min Çıktı = " + realMin);
	    System.out.println("Gerçek FCL Max Çıktı = " + realMax);

	    return new double[]{ realMin, realMax };
	}

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