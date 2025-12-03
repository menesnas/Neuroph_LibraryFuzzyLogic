package main;
import dataset.DataGenerator;
import java.util.Scanner;
import ml.ANNTrainer;

public class Main {

    public static void main(String[] args) {
        try {
            DataGenerator.generateDataset();
            ANNTrainer.loadConfig();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ANNTrainer.test10Networks();
        
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("======================================");
            System.out.println(" 1- Ağı Eğit ve Test Et (Momentumlu)");
            System.out.println(" 2- Ağı Eğit ve Test Et (Momentumsuz)");
            System.out.println(" 3- Ağı Eğit Epoch Göster (MAX 5000 epoch girebilirsiniz.)");
            System.out.println(" 4- Ağı Eğit ve Tekli Test (Momentumlu)");
            System.out.println(" 5- K-Fold Test");
            System.out.println(" 0- Çıkış");
            System.out.println("======================================");
            System.out.print("Seçiminiz: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    ANNTrainer.trainAndTestMomentum();
                    break;
                case 2:
                    ANNTrainer.trainAndTestNoMomentum();
                    break;
                case 3:
                    ANNTrainer.trainWithEpochShow();
                    break;
                case 4:
                    ANNTrainer.singlePredictionMomentum();
                    break;
                case 5:
                    ANNTrainer.kFoldTest();
                    break;
                case 0:
                    System.out.println("Çıkılıyor...");
                    return;
                default:
                    System.out.println("Geçersiz seçim!");
            }
        }
    }
}