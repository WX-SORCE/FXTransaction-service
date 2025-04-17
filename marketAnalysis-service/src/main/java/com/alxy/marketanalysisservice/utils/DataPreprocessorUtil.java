package com.alxy.marketanalysisservice.utils;

/**
 * @description: 数据预处理工具
 * @author: 宋枝波
 * @date: 2025-04-15 16:50
 */
public class DataPreprocessorUtil {
    //归一化方法
    public static double[][] normalize(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] normalized = new double[rows][cols];

        for (int j = 0; j < cols; j++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int i = 0; i < rows; i++) {
                if (data[i][j] < min) {
                    min = data[i][j];
                }
                if (data[i][j] > max) {
                    max = data[i][j];
                }
            }
            for (int i = 0; i < rows; i++) {
                normalized[i][j] = (data[i][j] - min) / (max - min);
            }
        }
        return normalized;
    }
    //反归一化方法
    public static double denormalize(double value, double min, double max) {
        return value * (max - min) + min;
    }
}