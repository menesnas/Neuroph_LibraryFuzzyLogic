package ml;

import java.util.Scanner;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import utils.FileUtils;

public class ANNTrainer {

    // Normalizasyon Sabitleri (DataGenerator ile AYNI olmalı)
    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 45;
    private static final double MIN_DAYLIGHT = 0;
    private static final double MAX_DAYLIGHT = 24;
    private static final double MIN_LOAD = 1000;
    private static final double MAX_LOAD = 5000;

    // Yardımcı Metot: Full dataseti yükler, karıştırır ve %75 Train - %25 Test olarak böler.
    private static DataSet[] getTrainTestSplit() {
        // 1. Ana veri setini yükle
        DataSet fullSet = FileUtils.loadCSV("datasets/full_dataset.csv", 2, 1);
        
        // 2. Veriyi karıştır (Rastgelelik şartı)
        fullSet.shuffle();
        
        // 3. Bölme oranını belirle (%75)
        int totalRows = fullSet.size();
        int trainCount = (int) (totalRows * 0.75);
        
        // 4. Alt veri setlerini oluştur
        DataSet trainSet = new DataSet(2, 1);
        DataSet testSet = new DataSet(2, 1);
        
        // 5. Satırları dağıt
        for (int i = 0; i < totalRows; i++) {
            DataSetRow row = fullSet.getRowAt(i);
            if (i < trainCount) {
                // Hata düzeltmesi: getRows().add kullanıldı
                trainSet.getRows().add(row);
            } else {
                testSet.getRows().add(row);
            }
        }
        
        return new DataSet[] { trainSet, testSet };
    }

    // ============================================================
    // 1) Ağı Eğit ve Test Et (Momentumlu)
    // ============================================================
    public static void trainAndTestMomentum() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];

        System.out.println("Veri Seti Hazırlandı: " + train.size() + " Eğitim, " + test.size() + " Test verisi.");

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 10, 1);

        MomentumBackpropagation mbp = new MomentumBackpropagation();
        mbp.setLearningRate(0.1);
        mbp.setMomentum(0.7);

        mlp.setLearningRule(mbp);
        
        System.out.println("Eğitim Başlıyor (Momentumlu)...");
        mlp.learn(train);

        double trainMSE = ANNUtils.computeMSE(mlp, train);
        double testMSE = ANNUtils.computeMSE(mlp, test);

        System.out.println("					===== Momentumlu Eğitim Sonucu =====");
        System.out.println("					Train MSE = " + trainMSE);
        System.out.println("					Test  MSE = " + testMSE);
    }

    // ============================================================
    // 2) Ağı Eğit ve Test Et (Momentumsuz)
    // ============================================================
    public static void trainAndTestNoMomentum() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];
        
        System.out.println("Veri Seti Hazırlandı: " + train.size() + " Eğitim, " + test.size() + " Test verisi.");

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 10, 1);

        BackPropagation bp = new BackPropagation();
        bp.setLearningRate(0.1);

        mlp.setLearningRule(bp);
        
        System.out.println("Eğitim Başlıyor (Momentumsuz)...");
        mlp.learn(train); 

        double trainMSE = ANNUtils.computeMSE(mlp, train);
        double testMSE = ANNUtils.computeMSE(mlp, test);

        System.out.println("					===== Momentumsuz Eğitim Sonucu =====");
        System.out.println("					Train MSE = " + trainMSE);
        System.out.println("					Test  MSE = " + testMSE);
    }

    // ============================================================
    // 3) Ağı Eğit Epoch Göster
    // ============================================================
    public static void trainWithEpochShow() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];
        DataSet test = split[1];

        Scanner sc = new Scanner(System.in);
        System.out.print("Eğitim kaç epoch sürsün? (Max 5000): ");
        int maxEpochs = sc.nextInt();

        // Kullanıcı 5000'den fazla girerse sınırı 5000'e çekelim
        if (maxEpochs > 5000) {
            System.out.println("! Uyarı: Maksimum sınır 5000'dir. 5000 epoch olarak ayarlandı.");
            maxEpochs = 5000;
        }

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 10, 1);

        BackPropagation bp = new BackPropagation();
        bp.setLearningRate(0.1);
        // Önemli: Her learn() çağrısı sadece 1 iterasyon yapacak, döngüyü biz elle kuruyoruz.
        bp.setMaxIterations(1); 

        mlp.setLearningRule(bp);

        System.out.println("					===== Epoch Gösterimi =====");

        // Döngü 1'den başlayıp kullanıcının girdiği sayıya kadar döner
        for (int epoch = 1; epoch <= maxEpochs; epoch++) {
            mlp.learn(train);

            double trainMSE = ANNUtils.computeMSE(mlp, train);
            double testMSE = ANNUtils.computeMSE(mlp, test); 

            System.out.println("					Epoch " + epoch + " | Train MSE = " + trainMSE + " | Test MSE = " + testMSE);
        }
    }

    // ============================================================
    // 4) Ağı Eğit ve Tekli Test (Momentumlu)
    // ============================================================
    public static void singlePredictionMomentum() {
        DataSet[] split = getTrainTestSplit();
        DataSet train = split[0];

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 10, 1);

        MomentumBackpropagation mbp = new MomentumBackpropagation();
        mbp.setLearningRate(0.1);
        mbp.setMomentum(0.7);

        mlp.setLearningRule(mbp);
        
        System.out.println("Ağ eğitiliyor, lütfen bekleyiniz...");
        mlp.learn(train);

        Scanner sc = new Scanner(System.in);

        System.out.println("\n--- TAHMİN MODÜLÜ ---");
        System.out.print("Sıcaklık (°C) giriniz: ");
        double rawTemp = sc.nextDouble();

        System.out.print("Gün ışığı süresi (saat) giriniz: ");
        double rawDaylight = sc.nextDouble();
        
        // --- 1. NORMALİZASYON (Girdiyi 0-1 arasına çekme) ---
        double normTemp = (rawTemp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
        double normDaylight = (rawDaylight - MIN_DAYLIGHT) / (MAX_DAYLIGHT - MIN_DAYLIGHT);
        
        // Ağa normalize edilmiş değerleri ver
        mlp.setInput(normTemp, normDaylight);
        mlp.calculate();

        // Ağdan normalize edilmiş çıktıyı al (0-1 arası)
        double normOutput = mlp.getOutput()[0];
        
        // --- 2. DENORMALİZASYON (Çıktıyı gerçek değere çevirme) ---
        double realOutput = (normOutput * (MAX_LOAD - MIN_LOAD)) + MIN_LOAD;

        System.out.println("-------------------------------------");
        System.out.println("Girilen Değerler (Ham): " + rawTemp + " °C, " + rawDaylight + " saat");
        System.out.println("Ağ Çıktısı (Normalize): " + normOutput);
        System.out.println(">>> TAHMİN EDİLEN TÜKETİM: " + String.format("%.2f", realOutput) + " kWh");
        System.out.println("-------------------------------------");
    }

    // ============================================================
    // 5) K-Fold Cross Validation
    // ============================================================
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

            MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 10, 1);
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

        System.out.println("					======================================");
        System.out.println("					ORTALAMA Train MSE : " + (totalTrainMSE / k));
        System.out.println("					ORTALAMA Test  MSE : " + (totalTestMSE / k));
        System.out.println("					======================================");
    }
}