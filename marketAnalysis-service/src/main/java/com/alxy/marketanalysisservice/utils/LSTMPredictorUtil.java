package com.alxy.marketanalysisservice.utils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
/**
 * @description: Deeplearning4j 库实现 LSTM 模型进行预测
 * @author: 宋枝波
 * @date: 2025-04-15 16:55
 */
public class LSTMPredictorUtil {

    public static double[] predictLSTM(double[][] data) {
        int nIn = 3;  // 输入特征数（汇率、买入价、卖出价）
        int nOut = 3; // 输出特征数
        int lstmLayerSize = 20;
        int numEpochs = 100;
        int nTimesteps = 1; // 每个样本仅1个时间步

        // 正确输入形状应为 [样本数, 特征数, 时间步长]
        INDArray input = Nd4j.create(data).reshape(data.length, nIn, nTimesteps);
        INDArray label = Nd4j.create(data).reshape(data.length, nIn, nTimesteps);

        // 配置神经网络
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam())
                .list()
                .layer(new LSTM.Builder()
                        .nIn(nIn)
                        .nOut(lstmLayerSize)
                        .activation(Activation.TANH)
                        .build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(lstmLayerSize)
                        .nOut(nOut)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        // 训练模型
        for (int i = 0; i < numEpochs; i++) {
            model.fit(input, label);
        }

        // 正确提取最后一个样本
        INDArray lastInput = input.get(NDArrayIndex.point(input.size(0) - 1), // 取最后一个样本
                NDArrayIndex.all(), // 保留所有特征
                NDArrayIndex.all()  // 保留所有时间步
        ).reshape(1, nIn, nTimesteps); // 重塑为 [1, 3, 1]

        // 预测
        INDArray output = model.output(lastInput);
        // 将输出重塑为一维数组
        INDArray reshapedOutput = output.reshape(nOut);
        return reshapedOutput.toDoubleVector();
    }
}