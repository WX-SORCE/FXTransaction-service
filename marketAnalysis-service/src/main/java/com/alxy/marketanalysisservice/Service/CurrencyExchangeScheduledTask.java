package com.alxy.marketanalysisservice.Service;

import com.alxy.marketanalysisservice.Entity.CurrencyHistory;
import com.alxy.marketanalysisservice.Repository.CurrencyHistoryRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CurrencyExchangeScheduledTask {

    private static final String LATEST_API_URL = "https://v6.exchangerate-api.com/v6/b262220b718ad78bab3c303f/latest/";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/b262220b718ad78bab3c303f/history/{base}/{year}/{month}/{day}";
    private static final List<String> BASE_CURRENCIES = Arrays.asList("USD", "CNY", "HKD", "EUR");
    private static final List<String> TARGET_CURRENCIES = Arrays.asList("USD", "CNY", "HKD", "EUR");
    private static final String DB_URL = "jdbc:mysql://localhost:3306/currency_exchange_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    private final RestTemplate restTemplate = new RestTemplate();
    @Resource
    private CurrencyHistoryRepository repository;

    // 每日零点执行（通过）
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyZeroTask() {
        for (String baseCurrency : BASE_CURRENCIES) {
            String url = LATEST_API_URL + baseCurrency;
            try {
                Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
                if (response != null && "success".equals(response.get("result"))) {
                    Map<String, Object> conversionRatesObj = (Map<String, Object>) response.get("conversion_rates");
                    if (conversionRatesObj != null) {
                        for (String targetCurrency : TARGET_CURRENCIES) {
                            if (baseCurrency.equals(targetCurrency)) {
                                continue;
                            }
                            if (conversionRatesObj.containsKey(targetCurrency)) {
                                try {
                                    Object rateObj = conversionRatesObj.get(targetCurrency);
                                    BigDecimal exchangeRate;
                                    if (rateObj instanceof Double) {
                                        exchangeRate = BigDecimal.valueOf((Double) rateObj);
                                    } else {
                                        exchangeRate = new BigDecimal(rateObj.toString());
                                    }
                                    saveToRealtime(baseCurrency, targetCurrency, exchangeRate);
                                    saveToHistory(baseCurrency, targetCurrency, exchangeRate);
                                } catch (NumberFormatException e) {
                                    System.err.println("Failed to parse exchange rate for " + baseCurrency + " to " + targetCurrency + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch exchange rates for " + baseCurrency + ": " + e.getMessage());
            }
        }
    }

    // 每小时执行一次
    @Scheduled(cron = "0 30 * * * ?")
    public void hourlyTask() {
        for (String baseCurrency : BASE_CURRENCIES) {
            String url = LATEST_API_URL + baseCurrency;
            try {
                Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
                if (response != null && "success".equals(response.get("result"))) {
                    Map<String, Object> conversionRatesObj = (Map<String, Object>) response.get("conversion_rates");
                    if (conversionRatesObj != null) {
                        for (String targetCurrency : TARGET_CURRENCIES) {
                            if (baseCurrency.equals(targetCurrency)) {
                                continue;
                            }
                            if (conversionRatesObj.containsKey(targetCurrency)) {
                                try {
                                    Object rateObj = conversionRatesObj.get(targetCurrency);
                                    BigDecimal exchangeRate;
                                    if (rateObj instanceof Double) {
                                        exchangeRate = BigDecimal.valueOf((Double) rateObj);
                                    } else {
                                        exchangeRate = new BigDecimal(rateObj.toString());
                                    }
                                    saveToRealtime(baseCurrency, targetCurrency, exchangeRate);
                                    updateHistoryHighLow(baseCurrency, targetCurrency, exchangeRate);
                                } catch (NumberFormatException e) {
                                    System.err.println("Failed to parse exchange rate for " + baseCurrency + " to " + targetCurrency + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch exchange rates for " + baseCurrency + ": " + e.getMessage());
            }
        }
    }

    // 每晚23:59执行
    @Scheduled(cron = "0 59 23 * * ?")
    public void daily2359Task() {
        for (String baseCurrency : BASE_CURRENCIES) {
            String url = LATEST_API_URL + baseCurrency;
            try {
                Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
                if (response != null && "success".equals(response.get("result"))) {
                    Map<String, Object> conversionRatesObj = (Map<String, Object>) response.get("conversion_rates");
                    if (conversionRatesObj != null) {
                        for (String targetCurrency : TARGET_CURRENCIES) {
                            if (baseCurrency.equals(targetCurrency)) {
                                continue;
                            }
                            if (conversionRatesObj.containsKey(targetCurrency)) {
                                try {
                                    Object rateObj = conversionRatesObj.get(targetCurrency);
                                    BigDecimal exchangeRate;
                                    if (rateObj instanceof Double) {
                                        exchangeRate = BigDecimal.valueOf((Double) rateObj);
                                    } else {
                                        exchangeRate = new BigDecimal(rateObj.toString());
                                    }
                                    updateHistoryClose(baseCurrency, targetCurrency, exchangeRate);
                                } catch (NumberFormatException e) {
                                    System.err.println("Failed to parse exchange rate for " + baseCurrency + " to " + targetCurrency + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch exchange rates for " + baseCurrency + ": " + e.getMessage());
            }
        }
    }

    // 时记录
    private void saveToRealtime(String baseCurrency, String targetCurrency, BigDecimal exchangeRate) {
        String id = UUID.randomUUID().toString();
        LocalDateTime date = LocalDateTime.now();
        String sql = "INSERT INTO currency_exchange_realtime (id, base_currency, target_currency, latest, date) " +
                "VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "base_currency = VALUES(base_currency), " +
                "target_currency = VALUES(target_currency), " +
                "latest = VALUES(latest), " +
                "date = VALUES(date)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, baseCurrency);
            pstmt.setString(3, targetCurrency);
            pstmt.setBigDecimal(4, exchangeRate);
            pstmt.setString(5, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save to realtime: " + e.getMessage());
        }
    }

    // 日记录
    private void saveToHistory(String baseCurrency, String targetCurrency, BigDecimal exchangeRate) {
        LocalDate date = LocalDate.now();
        String sql = "INSERT INTO currency_exchange_history (exchange_id,base_currency, target_currency, exchange_rate, high, low, open, close, date) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, baseCurrency);
            pstmt.setString(3, targetCurrency);
            pstmt.setBigDecimal(4, exchangeRate);
            pstmt.setBigDecimal(5, exchangeRate);
            pstmt.setBigDecimal(6, exchangeRate);
            pstmt.setBigDecimal(7, exchangeRate);
            pstmt.setBigDecimal(8, exchangeRate);
            pstmt.setString(9, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save to history: " + e.getMessage());
        }
    }

    // 更新日记录：High&Low
    private void updateHistoryHighLow(String baseCurrency, String targetCurrency, BigDecimal exchangeRate) {
        LocalDate date = LocalDate.now();
        String selectSql = "SELECT high, low FROM currency_exchange_history WHERE base_currency = ? AND target_currency = ? AND date = ?";
        String updateSql = "UPDATE currency_exchange_history SET high = ?, low = ? WHERE base_currency = ? AND target_currency = ? AND date = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {
                selectPstmt.setString(1, baseCurrency);
                selectPstmt.setString(2, targetCurrency);
                selectPstmt.setString(3, date.toString());
                try (ResultSet rs = selectPstmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal high = rs.getBigDecimal("high");
                        BigDecimal low = rs.getBigDecimal("low");
                        if (exchangeRate.compareTo(high) > 0) {
                            high = exchangeRate;
                        }
                        if (exchangeRate.compareTo(low) < 0) {
                            low = exchangeRate;
                        }
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                            updatePstmt.setBigDecimal(1, high);
                            updatePstmt.setBigDecimal(2, low);
                            updatePstmt.setString(3, baseCurrency);
                            updatePstmt.setString(4, targetCurrency);
                            updatePstmt.setString(5, date.toString());
                            updatePstmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to update history high/low: " + e.getMessage());
        }
    }

    // 更新日记录：Close
    private void updateHistoryClose(String baseCurrency, String targetCurrency, BigDecimal exchangeRate) {
        LocalDate date = LocalDate.now();
        String sql = "UPDATE currency_exchange_history SET close = ? WHERE base_currency = ? AND target_currency = ? AND date = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, exchangeRate);
            pstmt.setString(2, baseCurrency);
            pstmt.setString(3, targetCurrency);
            pstmt.setString(4, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update history close: " + e.getMessage());
        }
    }


    // 保存历史某一天的数据（保留）
    private void saveToDatabase(String baseCurrency, Map<String, Double> rates, LocalDate date) {
        for (String target : TARGET_CURRENCIES) {
            if(target.equals(baseCurrency)) {
                continue;
            }
            if (rates.containsKey(target)) {
                BigDecimal exchangeRate = BigDecimal.valueOf(rates.get(target));
                CurrencyHistory history = new CurrencyHistory();
                history.setExchangeId(UUID.randomUUID().toString());
                history.setBaseCurrency(baseCurrency);
                history.setTargetCurrency(target);
                history.setExchangeRate(exchangeRate);
                history.setHigh(exchangeRate.multiply(BigDecimal.valueOf(1.02)));
                history.setLow(exchangeRate.multiply(BigDecimal.valueOf(0.98)));
                history.setOpen(exchangeRate);
                history.setClose(exchangeRate);
                history.setMarketBuy(exchangeRate.multiply(BigDecimal.valueOf(0.96)));
                history.setMarketSell(exchangeRate.multiply(BigDecimal.valueOf(1.04)));
                history.setDate(date);
                repository.addCurrencyHistory(history);
            }
        }
    }

    // 获取最近三个月的汇率（预留）
    public void fetchLastThreeMonthsHistory() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 90; i++) {
            LocalDate date = today.minusDays(i);
            fetchAndSaveExchangeRatesForDate(date);
        }
    }

    // 获取并保存指定日期的汇率（保留）
    public void fetchAndSaveExchangeRatesForDate(LocalDate date) {
        for (String base : BASE_CURRENCIES) {
            System.out.printf("Fetching %s on %s...\n", base, date);
            Map<String, String> uriVars = new HashMap<>();
            uriVars.put("base", base);
            uriVars.put("year", String.valueOf(date.getYear()));
            uriVars.put("month", String.format("%02d", date.getMonthValue()));
            uriVars.put("day", String.format("%02d", date.getDayOfMonth()));

            try {
                Map<?, ?> response = restTemplate.getForObject(API_URL, Map.class, uriVars);
                if (response != null && "success".equals(response.get("result"))) {
                    Map<String, Object> rates = (Map<String, Object>) response.get("conversion_rates");

                    // 遍历汇率并确保值是 Double 类型
                    Map<String, Double> convertedRates = new HashMap<>();
                    for (Map.Entry<String, Object> entry : rates.entrySet()) {
                        if (entry.getValue() instanceof Integer) {
                            // 如果是 Integer 类型，强制转换为 Double
                            convertedRates.put(entry.getKey(), ((Integer) entry.getValue()).doubleValue());
                        } else if (entry.getValue() instanceof Double) {
                            // 如果已经是 Double 类型，则直接使用
                            convertedRates.put(entry.getKey(), (Double) entry.getValue());
                        }
                    }

                    saveToDatabase(base, convertedRates, date);
                    System.out.printf("Saved %s on %s\n", base, date);
                } else {
                    System.out.printf("Failed to get data for %s on %s\n", base, date);
                }
            } catch (Exception e) {
                System.out.printf("Error fetching %s on %s: %s\n", base, date, e.getMessage());
            }
        }
    }


    private final Random random = new Random();

    @Transactional
    public void updateBidAndAskPrices() {
        // 获取所有的汇率记录
        List<CurrencyHistory> allHistory = repository.findAll();

        for (CurrencyHistory history : allHistory) {
            // 获取当前的汇率
            BigDecimal exchangeRate = history.getExchangeRate();

            // 在当前汇率的基础上进行浮动，假设浮动范围是 ±0.1%
            BigDecimal bidMultiplier = BigDecimal.valueOf(0.995 + random.nextDouble() * 0.010); // 0.995 ~ 1.005
            BigDecimal askMultiplier = BigDecimal.valueOf(1.000 + random.nextDouble() * 0.010); // 1.000 ~ 1.010

            // 根据浮动范围计算买入价和卖出价
            BigDecimal bidPrice = exchangeRate.multiply(bidMultiplier).setScale(8, RoundingMode.HALF_UP);
            BigDecimal askPrice = exchangeRate.multiply(askMultiplier).setScale(8, RoundingMode.HALF_UP);

            // 更新实体类中的买入价和卖出价
            history.setMarketBuy(bidPrice);
            history.setMarketSell(askPrice);
        }

        // 保存更新后的所有记录
        repository.saveAll(allHistory);
    }

}    