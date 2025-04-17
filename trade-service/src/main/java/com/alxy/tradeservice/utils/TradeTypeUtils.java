package com.alxy.tradeservice.utils;

public class TradeTypeUtils {
    public static String getTradeTypeStr(Long tradeType) {
        if (tradeType == null) {
            return "未知交易类型";
        }
        switch (tradeType.intValue()) {
            case 0:
                return "MARKET_ORDER 市价单";
            case 1:
                return "LIMIT_ORDER 限价单";
            case 2:
                return "STOP_LOSS_ORDER 止损单";
            case 3:
                return "STOP_PROFIT_ORDER 止盈单";
            default:
                return "未知交易类型";
        }
    }
} 