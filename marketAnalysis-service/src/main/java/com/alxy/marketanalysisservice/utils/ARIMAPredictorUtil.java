package com.alxy.marketanalysisservice.utils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;


/**
 * @description: Smile 库实现 ARIMA 模型进行预测
 * @author: 宋枝波
 * @date: 2025-04-15 16:54
 */
public class ARIMAPredictorUtil {

    /**
     * 使用简化的 ARIMA(p, d, 0) 模型进行预测（这里仅实现差分和自回归部分）
     * @param data 时间序列数据，二维数组，每列代表一个时间序列
     * @param p 自回归阶数
     * @param d 差分阶数
     * @return 每个时间序列的预测值组成的数组
     */
    public static double[] predictARIMA(double[][] data, int p, int d) {
        int numSeries = data[0].length;
        double[] predictions = new double[numSeries];

        for (int seriesIndex = 0; seriesIndex < numSeries; seriesIndex++) {
            double[] singleSeries = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                singleSeries[i] = data[i][seriesIndex];
            }

            double[] differencedData = difference(singleSeries, d);
            int n = differencedData.length;
            if (n < p) {
                throw new IllegalArgumentException("差分后的数据长度必须大于自回归阶数 p");
            }

            // 构建自回归模型的设计矩阵 X 和响应向量 y
            RealMatrix X = new Array2DRowRealMatrix(n - p, p);
            RealVector y = new ArrayRealVector(n - p);

            for (int i = 0; i < n - p; i++) {
                for (int j = 0; j < p; j++) {
                    X.setEntry(i, j, differencedData[i + p - j - 1]);
                }
                y.setEntry(i, differencedData[i + p]);
            }

            // 使用最小二乘法进行线性回归
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y.toArray(), X.getData());
            double[] coefficients = regression.estimateRegressionParameters();

            // 对差分后的数据进行预测
            double differencedPrediction = 0;
            for (int i = 0; i < p; i++) {
                differencedPrediction += coefficients[i + 1] * differencedData[n - i - 1];
            }
            differencedPrediction += coefficients[0];

            // 反差分得到最终预测值
            predictions[seriesIndex] = inverseDifference(singleSeries, differencedPrediction, d);
        }

        return predictions;
    }

    /**
     * 对数据进行 d 阶差分
     * @param data 原始数据
     * @param d 差分阶数
     * @return 差分后的数据
     */
    private static double[] difference(double[] data, int d) {
        double[] differenced = data;
        for (int i = 0; i < d; i++) {
            double[] temp = new double[differenced.length - 1];
            for (int j = 1; j < differenced.length; j++) {
                temp[j - 1] = differenced[j] - differenced[j - 1];
            }
            differenced = temp;
        }
        return differenced;
    }

    /**
     * 对差分后的数据进行反差分
     * @param originalData 原始数据
     * @param differencedPrediction 差分后的预测值
     * @param d 差分阶数
     * @return 反差分后的预测值
     */
    private static double inverseDifference(double[] originalData, double differencedPrediction, int d) {
        double[] temp = originalData;
        for (int i = 0; i < d; i++) {
            double lastValue = temp[temp.length - 1];
            differencedPrediction += lastValue;
            temp = new double[temp.length + 1];
            System.arraycopy(originalData, 0, temp, 0, originalData.length);
            temp[temp.length - 1] = differencedPrediction;
        }
        return differencedPrediction;
    }
}