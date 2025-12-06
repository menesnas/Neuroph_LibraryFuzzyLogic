package ml;
import dataset.ModelCalculator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Scanner;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import utils.FileUtils;

public class ANNTrainer {
    
    public static int[] BEST_TOPOLOGY = {2, 10, 1}; 

    // Sabitler
    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 40;
    private static final double MIN_DAYLIGHT = 8;
    private static final double MAX_DAYLIGHT = 15;

    public static double MIN_LOAD = 1269; 
    public static double MAX_LOAD = 4438;

    public static void loadConfig() {
        try {
            java.io.File f = new java.io.File("datasets/config.txt");
            if (f.exists()) {
                java.util.Scanner sc = new java.util.Scanner(f);
                sc.useLocale(java.util.Locale.US); 
                
                if (sc.hasNextDouble()) MIN_LOAD = sc.nextDouble();
                if (sc.hasNextDouble()) MAX_LOAD = sc.nextDouble();
                
                System.out.println("-> ANNTrainer Config Yüklendi: Min=" + MIN_LOAD + " | Max=" + MAX_LOAD);
                sc.close();
            } else {
                System.out.println("! Config dosyası bulunamadı, varsayılan değerler kullanılacak.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dosyadan Min/Max okuyan metod
    private static void loadNormalizationConfig() {
        try (java.util.Scanner sc = new java.util.Scanner(new java.io.File("datasets/config.txt"))) {
            if (sc.hasNextDouble()) MIN_LOAD = sc.nextDouble();
            if (sc.hasNextDouble()) MAX_LOAD = sc.nextDouble();
            System.out.println("-> ANNTrainer Config Yüklendi: Min Load=" + MIN_LOAD + " | Max Load=" + MAX_LOAD);
        } catch (Exception e) {
            System.err.println("!!! HATA: datasets/config.txt okunamadı. Lütfen önce DataGenerator çalıştırın.");
            MIN_LOAD = 1269; 
            MAX_LOAD = 4438;
        }
    }
    static void saveEpochCSV(String fileName, double[] errors) {
        try (java.io.FileWriter fw = new java.io.FileWriter("datasets/" + fileName)) {
            for (int i = 0; i < errors.length; i++) {
                fw.write(i + ";" + errors[i] + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DataSet[] getTrainTestSplit() {
        DataSet fullSet = FileUtils.loadCSV("datasets/full_dataset.csv", 2, 1);
        fullSet.shuffle();
        
        // (%75)
        int totalRows = fullSet.size();
        int trainCount = (int) (totalRows * 0.75);
        
        DataSet trainSet = new DataSet(2, 1);
        DataSet testSet = new DataSet(2, 1);
        
        for (int i = 0; i < totalRows; i++) {
            DataSetRow row = fullSet.getRowAt(i);
            if (i < trainCount) {
                trainSet.getRows().add(row);
            } else {
                testSet.getRows().add(row);
            }
        }
        
        return new DataSet[] { trainSet, testSet };
    }

    // Ağı Eğit ve Test Et (MOMENTUMLU)
    public static void trainAndTestMomentum() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];

        System.out.println("Veri Seti Hazırlandı: " + train.size() + " Eğitim, " + test.size() + " Test verisi.");

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(BEST_TOPOLOGY);

        MomentumBackpropagation mbp = new MomentumBackpropagation();
        mbp.setLearningRate(0.1);
        mbp.setMomentum(0.7);

        mlp.setLearningRule(mbp);
        
        System.out.println("Eğitim Başlıyor (Momentumlu)...");
        mlp.learn(train);

        double trainMSE = ANNUtils.computeMSE(mlp, train);
        double testMSE = ANNUtils.computeMSE(mlp, test);

        System.out.println("===== Momentumlu Eğitim Sonucu =====");
        System.out.println("Train MSE = " + trainMSE);
        System.out.println("Test  MSE = " + testMSE);
    }

    // Ağı Eğit ve Test Et (MOMENTUMSUZ)
    public static void trainAndTestNoMomentum() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];
        
        System.out.println("Veri Seti Hazırlandı: " + train.size() + " Eğitim, " + test.size() + " Test verisi.");

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(BEST_TOPOLOGY);

        BackPropagation bp = new BackPropagation();
        bp.setLearningRate(0.1);

        mlp.setLearningRule(bp);
        
        System.out.println("Eğitim Başlıyor (Momentumsuz)...");
        mlp.learn(train); 

        double trainMSE = ANNUtils.computeMSE(mlp, train);
        double testMSE = ANNUtils.computeMSE(mlp, test);

        System.out.println("===== Momentumsuz Eğitim Sonucu =====");
        System.out.println("Train MSE = " + trainMSE);
        System.out.println("Test  MSE = " + testMSE);
    }

    
    // Ağı Eğit Epoch Göster
    public static void trainWithEpochShow() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];

        Scanner sc = new Scanner(System.in);
        System.out.print("Eğitim kaç epoch sürsün? (Max 5000): ");
        int maxEpochs = sc.nextInt();

        // 5000'den fazla girilirse 5000 olur 
        if (maxEpochs > 5000) {
            System.out.println("! Uyarı: Maksimum sınır 5000'dir. 5000 epoch olarak ayarlandı.");
            maxEpochs = 5000;
        }

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(BEST_TOPOLOGY);

        BackPropagation bp = new BackPropagation();
        bp.setLearningRate(0.1);
        bp.setMaxIterations(1); 

        mlp.setLearningRule(bp);

        System.out.println("===== Epoch Gösterimi =====");

        for (int epoch = 1; epoch <= maxEpochs; epoch++) {
            mlp.learn(train);

            double trainMSE = ANNUtils.computeMSE(mlp, train);
            double testMSE = ANNUtils.computeMSE(mlp, test); 

            System.out.println(String.format("	Epoch %d | Train MSE = %.9f | Test MSE = %.9f", epoch, trainMSE, testMSE));        }
    }


 // Ağı Eğit ve Tekli Test (MOMENTUMLU)
    public static void singlePredictionMomentum() {
        
        // !!! EKLENEN SATIR BURASI !!!
        // Tahmin yapmadan önce güncel Min/Max değerlerini dosyadan oku
        loadConfig(); 

        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];

        // 1. Ağı Oluştur (Önceki adımda bulunan EN İYİ topolojiyi kullanıyoruz)
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(BEST_TOPOLOGY);

        // 2. Momentum Ayarları
        MomentumBackpropagation mbp = new MomentumBackpropagation();
        mbp.setLearningRate(0.1);
        mbp.setMomentum(0.7);

        mlp.setLearningRule(mbp);
        
        System.out.println("Ağ eğitiliyor, lütfen bekleyiniz...");
        mlp.learn(train); // Ağı eğit

        Scanner sc = new Scanner(System.in);

        System.out.println("\n--- TAHMİN MODÜLÜ ---");
        System.out.println("NOT: Ondalıklı sayıları nokta (.) değil virgül (,) ile giriniz. Örn: 10,5");
        
        System.out.print("Sıcaklık (°C) giriniz: ");
        double rawTemp = sc.nextDouble();

        System.out.print("Gün ışığı süresi (saat) giriniz: ");
        double rawDaylight = sc.nextDouble();
        
        // --- NORMALİZASYON ADIMI ---
        double normTemp = (rawTemp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
        double normDaylight = (rawDaylight - MIN_DAYLIGHT) / (MAX_DAYLIGHT - MIN_DAYLIGHT);
        
        // Ağa normalize edilmiş değerleri ver
        mlp.setInput(normTemp, normDaylight);
        
        // Ağı çalıştır (Hesapla)
        mlp.calculate();

        // Ağdan normalize edilmiş çıktıyı al (0 ile 1 arasında bir sayıdır)
        double normOutput = mlp.getOutput()[0];
        
        // --- DENORMALİZASYON ADIMI ---
        // (loadConfig() sayesinde MIN_LOAD ve MAX_LOAD artık güncel)
        double realOutput = normOutput * (MAX_LOAD - MIN_LOAD) + MIN_LOAD;

        System.out.println("-------------------------------------");
        System.out.println("Girilen Değerler (Ham): " + rawTemp + " °C, " + rawDaylight + " saat");
        System.out.println(">>> TAHMİN EDİLEN TÜKETİM: " + String.format("%.2f", realOutput) + " kWh");
        System.out.println("-------------------------------------");
    }

    
    // K-Fold Cross Validation
    public static void kFoldTest() {
        Scanner sc = new Scanner(System.in);
        System.out.print("K değerini giriniz: ");
        int k = sc.nextInt();

        DataSet full = FileUtils.loadCSV("datasets/full_dataset.csv", 2, 1);
        full.shuffle(); 

        int dataSize = full.size();
        int foldSize = dataSize / k;

        double totalTrainMSE = 0;
        double totalTestMSE = 0;

        System.out.println("===== " + k + "-Fold Cross Validation Başladı =====");

        for (int fold = 0; fold < k; fold++) {
            DataSet trainSet = new DataSet(2, 1);
            DataSet testSet = new DataSet(2, 1);

            int start = fold * foldSize;
            int end = start + foldSize;
            
            if (fold == k - 1) end = dataSize;

            for (int i = 0; i < dataSize; i++) {
                DataSetRow row = full.getRowAt(i);
                if (i >= start && i < end) {
                    testSet.getRows().add(row);
                } else {
                    trainSet.getRows().add(row);
                }
            }

            MultiLayerPerceptron mlp = new MultiLayerPerceptron(BEST_TOPOLOGY);
            BackPropagation bp = new BackPropagation();
            bp.setLearningRate(0.1);
            mlp.setLearningRule(bp);
            
            mlp.learn(trainSet);

            double trainMSE = ANNUtils.computeMSE(mlp, trainSet);
            double testMSE = ANNUtils.computeMSE(mlp, testSet);

            totalTrainMSE += trainMSE;
            totalTestMSE += testMSE;

            System.out.println("Fold " + (fold + 1) + " | Train MSE = " + trainMSE + " | Test MSE = " + testMSE);
        }

        System.out.println("	======================================");
        System.out.println("	ORTALAMA Train MSE : " + (totalTrainMSE / k));
        System.out.println("	ORTALAMA Test  MSE : " + (totalTestMSE / k));
        System.out.println("	======================================");
    }

	 // 10 Farklı Ağ Topolojisi Denemesi
    public static void test10Networks() {

        System.out.println(">>> 10 farklı ağ topolojisi epoch epoch test edilecek KAÇ EPOCH GİRİLSİN?...");

        int[][] architectures = new int[][]{
            {2, 3, 1},
            {2, 5, 1},
            {2, 7, 1},
            {2, 10, 1},
            {2, 15, 1},
            {2, 5, 3, 1},
            {2, 8, 4, 1},
            {2, 12, 6, 1},
            {2, 20, 10, 1},
            {2, 30, 15, 1}
        };

        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];
        Scanner sc = new Scanner(System.in);
        System.out.print("EPOCH değerini giriniz (MAX 5.000): ");
        int maxEpochs = sc.nextInt();
     // 5000'den fazla girilirse 5000 olur 
        if (maxEpochs > 5000) {
            System.out.println("! Uyarı: Maksimum sınır 5000'dir. 5000 epoch olarak ayarlandı.");
            maxEpochs = 5000;
        }

        List<double[]> allTrainErrors = new ArrayList<>();
        List<double[]> allTestErrors = new ArrayList<>();

        double bestTestMSE = Double.MAX_VALUE;
        int[] bestTopology = null;

        for (int i = 0; i < architectures.length; i++) {

            int[] arch = architectures[i];
            System.out.println("Ağ #" + (i+1) + " Topoloji: " + Arrays.toString(arch));

            MultiLayerPerceptron mlp = new MultiLayerPerceptron(arch);
            BackPropagation bp = new BackPropagation();
            bp.setLearningRate(0.1);
            bp.setMaxIterations(1); // manuel epoch kontrolü

            mlp.setLearningRule(bp);

            double[] trainErrors = new double[maxEpochs];
            double[] testErrors = new double[maxEpochs];

            for (int epoch = 0; epoch < maxEpochs; epoch++) {
                mlp.learn(train);

                double trainMSE = ANNUtils.computeMSE(mlp, train);
                double testMSE = ANNUtils.computeMSE(mlp, test);

                trainErrors[epoch] = trainMSE;
                testErrors[epoch] = testMSE;
            }

            allTrainErrors.add(trainErrors);
            allTestErrors.add(testErrors);

            // En iyi modeli seç
            double finalTest = testErrors[maxEpochs - 1];
            if (finalTest < bestTestMSE) {
                bestTestMSE = finalTest;
                bestTopology = arch;
            }
        }

        System.out.println("\n===== EN İYİ TOPOLOJİ =====");
        System.out.println("Topoloji: " + Arrays.toString(bestTopology));
        
        BEST_TOPOLOGY = bestTopology; 

        System.out.println("============================");
        FileUtils.exportEpochErrors(allTrainErrors, allTestErrors);

        System.out.println("\nEpoch hataları 'epoch_results.csv' dosyasına yazıldı.");
        utils.GraphPlotter.drawAndSaveGraph();
    }


}