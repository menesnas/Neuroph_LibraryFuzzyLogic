package ml;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

public class ANNUtils {

    public static double computeMSE(NeuralNetwork<?> net, DataSet data) {
        double mse = 0;
        for (DataSetRow row : data.getRows()) {
            net.setInput(row.getInput());
            net.calculate();
            double out = net.getOutput()[0];
            double expected = row.getDesiredOutput()[0];
            mse += Math.pow(expected - out, 2);
        }
        return mse / data.size();
    }
}
