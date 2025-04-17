package com.alxy.marketanalysisservice.Service;

import com.alxy.marketanalysisservice.Entity.CurrencyHistory;
import com.alxy.marketanalysisservice.Repository.CurrencyHistoryRepository;

import com.alxy.marketanalysisservice.utils.ARIMAPredictorUtil;
import com.alxy.marketanalysisservice.utils.DataPreprocessorUtil;
import com.alxy.marketanalysisservice.utils.LSTMPredictorUtil;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 历史货币数据分析
 * @author: 宋枝波
 * @date: 2025-04-11 09:39
 */
@Service
public class MarketAnalysisService {
    //AI秘钥
    public  static final String apiKey ="";


    @Autowired
    private CurrencyHistoryRepository currencyExchangeHistoryRepository;

    // 计算指定货币对在特定时间段内的平均汇率
    public BigDecimal calculateAverageRate(String baseCurrency, String targetCurrency) {
        // 获取当前日期
        Date endDate = new Date();
        // 计算29天前的日期
        Date startDate = new Date(endDate.getTime() - 29 * 24 * 60 * 60 * 1000);
        List<CurrencyHistory> histories = currencyExchangeHistoryRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate);
        if (histories.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalRate = BigDecimal.ZERO;
        // 计算总汇率
        for (CurrencyHistory history : histories) {
            totalRate = totalRate.add(history.getExchangeRate());
        }
        //计算平局汇率，保留8位小数，四舍五入
        return totalRate.divide(BigDecimal.valueOf(histories.size()), 8, RoundingMode.HALF_UP);
    }

    // 计算指定货币对在特定时间段内的汇率波动范围
    public BigDecimal calculateVolatility(String baseCurrency, String targetCurrency) {
        // 获取当前日期
        Date endDate = new Date();
        // 计算29天前的日期
        Date startDate = new Date(endDate.getTime() - 29 * 24 * 60 * 60 * 1000);
        List<CurrencyHistory> histories = currencyExchangeHistoryRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate);
        if (histories.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal maxHigh = BigDecimal.ZERO;
        BigDecimal minLow = BigDecimal.valueOf(Double.MAX_VALUE);
        //遍历获取最高汇率值和最低汇率
        for (CurrencyHistory history : histories) {
            if (history.getHigh().compareTo(maxHigh) > 0) {
                maxHigh = history.getHigh();
            }
            if (history.getLow().compareTo(minLow) < 0) {
                minLow = history.getLow();
            }
        }
        //得
        return maxHigh.subtract(minLow);
    }

    // 判断指定货币对在特定时间段内的汇率趋势
    public String determineTrend(String baseCurrency, String targetCurrency) {
        // 获取当前日期
        Date endDate = new Date();
        // 计算29天前的日期
        Date startDate = new Date(endDate.getTime() - 29 * 24 * 60 * 60 * 1000);
        List<CurrencyHistory> histories = currencyExchangeHistoryRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate);
        if (histories.size() < 2) {
            return "数据不足，无法判断趋势";
        }
        BigDecimal firstClose = histories.get(0).getClose();
        BigDecimal lastClose = histories.get(histories.size() - 1).getClose();
        if (lastClose.compareTo(firstClose) > 0) {
            return "汇率呈上升趋势";
        } else if (lastClose.compareTo(firstClose) < 0) {
            return "汇率呈下降趋势";
        } else {
            return "汇率保持稳定";
        }
    }

    //ai分析接口
    // 接入 GPT 分析货币走势并给出意见
    public String analyzeCurrencyTrendWithGPT(String baseCurrency, String targetCurrency) {
        // 获取当前日期
        Date endDate = new Date();
        // 计算29天前的日期
        Date startDate = new Date(endDate.getTime() - 29 * 24 * 60 * 60 * 1000);
        List<CurrencyHistory> histories = currencyExchangeHistoryRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate);
        if (histories.isEmpty()) {
            return "没有可用的历史数据进行分析。";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder dataSummary = new StringBuilder();
        dataSummary.append("从 ").append(sdf.format(startDate)).append(" 到 ").append(sdf.format(endDate))
                .append("，货币对 ").append(baseCurrency).append("/").append(targetCurrency).append(" 的历史数据如下：\n");
        for (CurrencyHistory history : histories) {
            dataSummary.append("日期: ").append(sdf.format(history.getDate()))
                    .append("，汇率: ").append(history.getExchangeRate())
                    .append("，最高: ").append(history.getHigh())
                    .append("，最低: ").append(history.getLow())
                    .append("，开盘: ").append(history.getOpen())
                    .append("，收盘: ").append(history.getClose()).append("\n");
        }


        OpenAiService service = new OpenAiService(apiKey);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt("根据目前世界局势，金融市场趋势与变化，请分析以下货币对 " + baseCurrency + "/" + targetCurrency + " 的走势并给出投资意见：\n" + dataSummary.toString())
                .maxTokens(200)
                .temperature(0.7)
                .build();

        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        if (!choices.isEmpty()) {
            return choices.get(0).getText().trim();
        } else {
            return "未能从 GPT 获取有效分析结果。";
        }
    }

    // 获取指定基准货币和目标货币近一个月的汇率历史数据
    public List<CurrencyHistory> fetchOneMonthHistory(String baseCurrency, String targetCurrency) {
        // 获取当前日期
        Date endDate = new Date();
        // 计算29天前的日期
        Date startDate = new Date(endDate.getTime() - 29 * 24 * 60 * 60 * 1000);
        // 定义日期格式
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        // 解析日期字符串为 LocalDate 对象
//        LocalDate startLocalDate = LocalDate.parse("2025-10-01", formatter);
//        LocalDate endLocalDate = LocalDate.parse("2025-10-30", formatter);

//        // 将 LocalDate 转换为 Date 对象
//        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        // 从数据库中查询符合条件的汇率历史数据
        return currencyExchangeHistoryRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate
        );

    }

    // 从汇率历史数据列表中提取汇率、买入价和卖出价
    public double[][] extractRates(List<CurrencyHistory> historyList) {
        double[][] rates = new double[historyList.size()][3];
        for (int i = 0; i < historyList.size(); i++) {
            rates[i][0] = historyList.get(i).getExchangeRate().doubleValue();
            rates[i][1] = historyList.get(i).getMarketBuy().doubleValue();
            rates[i][2] = historyList.get(i).getMarketSell().doubleValue();
        }
        return rates;
    }

    // 使用机器学习预测汇率、买入价和卖出价走势
    public Map<String, Double> predictExchangeRate(String baseCurrency, String targetCurrency) {
        List<CurrencyHistory> historyList = fetchOneMonthHistory(baseCurrency, targetCurrency);
        double[][] exchangeRates = extractRates(historyList);

        double[][] normalizedRates = DataPreprocessorUtil.normalize(exchangeRates);

        // 使用 ARIMA 进行预测
        int p = 1, d = 1;
        double[] arimaPredictions = ARIMAPredictorUtil.predictARIMA(normalizedRates, p, d);

        // 使用 LSTM 进行预测
        double[] lstmPredictions = LSTMPredictorUtil.predictLSTM(normalizedRates);

        // 简单平均两种预测结果
        double[] finalPredictions = new double[3];
        for (int i = 0; i < 3; i++) {
            finalPredictions[i] = (arimaPredictions[i] + lstmPredictions[i]) / 2;
        }

        // 反归一化
        double[] min = new double[3];
        double[] max = new double[3];
        for (int i = 0; i < 3; i++) {
            min[i] = Double.MAX_VALUE;
            max[i] = Double.MIN_VALUE;
        }
        for (double[] rate : exchangeRates) {
            for (int i = 0; i < 3; i++) {
                if (rate[i] < min[i]) {
                    min[i] = rate[i];
                }
                if (rate[i] > max[i]) {
                    max[i] = rate[i];
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            finalPredictions[i] = DataPreprocessorUtil.denormalize(finalPredictions[i], min[i], max[i]);
        }

        // 将结果存入 Map
        Map<String, Double> resultMap = new HashMap<>();
        resultMap.put("汇率", finalPredictions[0]);
        resultMap.put("买入价", finalPredictions[1]);
        resultMap.put("卖出价", finalPredictions[2]);

        return resultMap;
    }
    public List<CurrencyHistory> findCurrentExchangeHistory() {
        return currencyExchangeHistoryRepository.findAll();
    }

    public  CurrencyHistory getNewRates(String baseCurrency,String targetCurrency){
        return currencyExchangeHistoryRepository.findLatestByBaseCurrencyAndTargetCurrency(baseCurrency,targetCurrency).get();
    }
}