package com.alxy.tradeservice.service.impl;


import com.alxy.tradeservice.entity.Account;
import com.alxy.tradeservice.entity.FundsHistory;
import com.alxy.tradeservice.entity.Transaction;
import com.alxy.tradeservice.entity.dto.BuyRequestDto;
import com.alxy.tradeservice.entity.dto.Result;
import com.alxy.tradeservice.entity.vo.TradeVo;
import com.alxy.tradeservice.feign.FeignAccount;
import com.alxy.tradeservice.feign.RiskFeign;
import com.alxy.tradeservice.repository.FundsHistoryRepository;
import com.alxy.tradeservice.repository.TransactionRepository;
import com.alxy.tradeservice.service.ForexTradingService;
import com.alxy.tradeservice.utils.ThreadLocalUtil;
import com.alxy.tradeservice.utils.TradeTypeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author 李尚奇
 * @date 2025/4/15
 */
@Service
public class ForexTradingServiceImpl implements ForexTradingService {

    // 注入交易数据访问接口
    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private FundsHistoryRepository fundsHistoryRepository;

    @Resource
    private RiskFeign riskFeign;

    @Autowired
    private FeignAccount accountFeign;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate; // Kafka 消息模板

    // 存储挂单信息
    private Map<String, Transaction> pendingOrders = new HashMap<>();
    @Autowired
    TradeServiceImpl tradeService;

    /**
     * 生成订单，并判断订单类型执行相应的操作处理
     *
     * @param buyRequest
     * @return
     */
    public Transaction processOrder(BuyRequestDto buyRequest) {
        //生成唯一的交易订单
        String transactionId = UUID.randomUUID().toString();
        //获取交易时的汇率
        BigDecimal exchangeRate = buyRequest.getTotal();
        if(buyRequest.getLeverageRatio()!=null){
            Transaction transaction = tradeService.pendingTransaction(buyRequest);
            return transaction;
        }
                //根据订单类型执行相应的处理
                switch (buyRequest.getTradeType().intValue()) {
                    case 0:
                        return processMarketOrder(transactionId, buyRequest, exchangeRate);
                    case 1:
                        return processLimitOrder(transactionId, buyRequest, exchangeRate);
                    case 2:
                        return processStopLossOrder(transactionId, buyRequest, exchangeRate);
                    case 3:
                        return processStopProfitOrder(transactionId, buyRequest, exchangeRate);
                    default:
                        Transaction failedTransaction = buildTransaction(transactionId, buyRequest, "FAILED", BigDecimal.ZERO);
                        return transactionRepository.save(failedTransaction);
                }
    }

    /**
     * 生成市价单
     * @param transactionId
     * @param buyRequest
     * @param exchangeRate
     * @return
     */
    private Transaction processMarketOrder(String transactionId, BuyRequestDto buyRequest, BigDecimal exchangeRate) {
        if ("buy".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return marketBuyRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("sell".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return marketSellRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Transaction failedTransaction = buildTransaction(transactionId, buyRequest, "FAILED", BigDecimal.ZERO);
        return transactionRepository.save(failedTransaction);
    }

    /**
     *  生成限价单
     * @param transactionId
     * @param buyRequest
     * @param exchangeRate
     * @return
     */
    private Transaction processLimitOrder(String transactionId, BuyRequestDto buyRequest, BigDecimal exchangeRate) {
        if ("buy".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return limitBuyRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("sell".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return limitSellRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Transaction failedTransaction = buildTransaction(transactionId, buyRequest, "FAILED", BigDecimal.ZERO);
        return transactionRepository.save(failedTransaction);
    }

    /**
     *  生成止损单
     * @param transactionId
     * @param buyRequest
     * @param exchangeRate
     * @return
     */
    private Transaction processStopLossOrder(String transactionId, BuyRequestDto buyRequest, BigDecimal exchangeRate) {
        if ("buy".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return stopLossBuyRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("sell".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return stopLossSellRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Transaction failedTransaction = buildTransaction(transactionId, buyRequest, "FAILED", BigDecimal.ZERO);
        return transactionRepository.save(failedTransaction);
    }

    /** 生成止盈单
     * @param transactionId
     * @param buyRequest
     * @param exchangeRate
     * @return
     */
    private Transaction processStopProfitOrder(String transactionId, BuyRequestDto buyRequest, BigDecimal exchangeRate) {
        if ("buy".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return stopPorfitBuyRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if ("sell".equalsIgnoreCase(buyRequest.getAction())) {
            try {
                return stopProfitSellRequest(buyRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Transaction failedTransaction = buildTransaction(transactionId, buyRequest, "FAILED", BigDecimal.ZERO);
        return transactionRepository.save(failedTransaction);
    }

    /** 构建失败交易
     * @param transactionId
     * @param buyRequest
     * @param failed
     * @param zero
     * @return
     */
    private Transaction buildTransaction(String transactionId, BuyRequestDto buyRequest, String failed, BigDecimal zero) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId(buyRequest.getUserId());
        transaction.setBaseCurrency(buyRequest.getBaseCurrency());
        transaction.setTargetCurrency(buyRequest.getTargetCurrency());
        transaction.setAction(buyRequest.getAction());
        transaction.setAmount(zero);
        transaction.setStatus(failed);
        transaction.setTransactionTime(buyRequest.getTransactionTime());

        //保存交易记录到数据库
        transactionRepository.save(transaction);
        return transaction;
    }


    /**
     * 市价单买入
     * @param buyRequest
     * @return
     * @throws Exception
     */
    @Transactional
    public Transaction marketBuyRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Map<String, Object> claims = ThreadLocalUtil.get();
//        String userId = claims.get("userId").toString();
//        buyRequest.setUserId(userId);
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);

//        if(monitoring == 0){
        if(true){
            //2、生成订单
            Transaction transaction = createTransaction(buyRequest);

            // 计算交易成本
            // 3. 计算交易金额和手续费（示例：手续费 0.1%）
            BigDecimal transactionAmount = buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize()));
            BigDecimal fee = transactionAmount.multiply(BigDecimal.valueOf(0.001)); // 0.1% 手续费
            BigDecimal slippageFee = transactionAmount.multiply(BigDecimal.valueOf(0.0005)); // 0.05% 滑点费
            BigDecimal totalFees = fee.add(slippageFee);
            BigDecimal baseCurrencyDeduction =  BigDecimal.valueOf(buyRequest.getSize()).add(totalFees); // 扣除的基础货币（含手续费和滑点费）
            BigDecimal targetCurrencyAddition = transactionAmount; // 获得的目标货币

            // 4. 封装账户操作参数（TradeVo）
            TradeVo tradeVo = new TradeVo();
            tradeVo.setUserId(buyRequest.getUserId());
            tradeVo.setBaseCurrency(buyRequest.getBaseCurrency());
            tradeVo.setTargetCurrency(buyRequest.getTargetCurrency());
            tradeVo.setBaseBalance(baseCurrencyDeduction); // 基础货币扣除金额
            tradeVo.setTargetBalance(targetCurrencyAddition); // 目标货币增加金额

            //5、调用账户模块校验余额、扣除资金
            // 调用Account服务，进行客户账户的更新操作
//            Result<?> result = accountFeign.exchange(tradeVo);
//            if(result.getCode() != 200) {
//                transaction.setStatus("FAILED");
//                throw new RuntimeException("账户余额更新失败");
//            }

            // 6. 记录资金流水，记录资金流水（funds_history 表），类型为 withdrawal，备注 “外汇买入”
            recordFundsHistory(buyRequest.getUserId(), baseCurrencyDeduction, "外汇买入"+buyRequest.getTradeType(), transaction.getTransactionId());

            // 7、更新订单状态为成交
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);

            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(transaction, buyRequest);

        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }

        return null;
    }

    /**
     * 市价单卖出
     * @param buyRequest
     * @return
     */
    @Transactional
    public Transaction marketSellRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Map<String, Object> claims = ThreadLocalUtil.get();
//        String userId = claims.get("userId").toString();
//        buyRequest.setUserId(userId);
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();


//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            //2、生成订单
            Transaction transaction = createTransaction(buyRequest);

            // 3. 计算交易成本
            BigDecimal transactionAmount = buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize()));
            BigDecimal fee = transactionAmount.multiply(BigDecimal.valueOf(0.001)); // 0.1% 手续费
            BigDecimal slippageFee = transactionAmount.multiply(BigDecimal.valueOf(0.0005)); // 0.05% 滑点费
            BigDecimal totalFees = fee.add(slippageFee);
            BigDecimal baseCurrencyAddition = transactionAmount.subtract(totalFees); // 增加的基础货币（扣除手续费和滑点费）
            BigDecimal targetCurrencyDeduction = BigDecimal.valueOf(buyRequest.getSize()); // 扣除的目标货币

            // 4. 封装账户操作参数（TradeVo）
            TradeVo tradeVo = new TradeVo();
            tradeVo.setUserId(buyRequest.getUserId());
            tradeVo.setBaseCurrency(buyRequest.getBaseCurrency());
            tradeVo.setTargetCurrency(buyRequest.getTargetCurrency());
            tradeVo.setBaseBalance(baseCurrencyAddition); // 基础货币增加金额
            tradeVo.setTargetBalance(targetCurrencyDeduction); // 目标货币扣除金额

//            // 调用Account服务，进行客户账户的更新操作
//            Result<?> result = accountFeign.exchange(tradeVo);
//            if(result.getCode() != 200) {
//                transaction.setStatus("FAILED");
//                throw new RuntimeException("账户余额更新失败");
//            }

            // 6. 记录资金流水，类型为 deposit，备注 “外汇卖出”
            recordFundsHistory(buyRequest.getUserId(), baseCurrencyAddition, "外汇卖出" + buyRequest.getTradeType(), transaction.getTransactionId());

            // 7. 更新订单状态为成交
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);

            // 8. 发送通知（Kafka + WebSocket）
            sendTransactionNotification(transaction, buyRequest);
            return transaction;
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    @Transactional
    public Transaction limitBuyRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    @Transactional
    public Transaction limitSellRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    @Transactional
    public Transaction stopLossBuyRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    private Transaction stopLossSellRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    @Transactional
    public Transaction stopPorfitBuyRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    @Transactional
    public Transaction stopProfitSellRequest(BuyRequestDto buyRequest) throws Exception {
        // 触发风控模块进行风险评估    检查保证金、持仓限额
        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequest.getUserId(), buyRequest.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequest.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequest.getAction());
//        request.setAmount(buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize())));
//        int monitoring = riskFeign.monitoring(request);
//
//        if(monitoring == 0){
        if(true){
            // 2. 生成挂单交易记录
            Transaction pendingOrder = createPendingOrder(buyRequest);

            // 3、保存挂单信息
            //获取订单信息，保存挂单
            pendingOrders.put(pendingOrder.getTransactionId(), pendingOrder);

            // 保存交易记录到数据库
            transactionRepository.save(pendingOrder);
            // 8、 发送通知（Kafka + WebSocket）
            sendTransactionNotification(pendingOrder, buyRequest);
            return Transaction.builder().build();
        }
//        else if (monitoring == 1) {
//            throw new Exception("年度累计交易金额超限，终止交易");
//        } else if (monitoring == 2) {
//            throw  new Exception("年度累计交易次数超限，终止交易");
//        }
        return null;

    }

    //创建挂起订单
    private Transaction createPendingOrder(BuyRequestDto buyRequest) {
        Transaction.TransactionBuilder builder = Transaction.builder().
                transactionId(String.valueOf(UUID.randomUUID()))
                .userId(buyRequest.getUserId())
                .price(buyRequest.getPrice())
                .transactionTime(new Date())
                .transactionType("automated")
                .status("PENDING")
                .action(TradeTypeUtils.getTradeTypeStr(buyRequest.getTradeType()))
                .baseCurrency(buyRequest.getBaseCurrency())
                .targetCurrency(buyRequest.getTargetCurrency());


        if (isStopLossOrder(buyRequest.getTradeType())) {
            builder.stopLoss(buyRequest.getPendingOrderBuy());
        } else if (isTakeProfitOrder(buyRequest.getTradeType())) {
            builder.takeProfit(buyRequest.getPendingOrderBuy());
        }
        Transaction transaction = builder.build();

        return transaction;
    }

    private boolean isStopLossOrder(Long tradeType) {
        return 1L == tradeType || 2L == tradeType; // adjust as needed
    }

    private boolean isTakeProfitOrder(Long tradeType) {
        return 4L == tradeType; // adjust as needed
    }
    //定时器：监控限价单的执行时机
//    @Scheduled(fixedRate = 5000) // 每5秒检查一次
//    public void checkPendingOrders() {
//        for (Map.Entry<String, Transaction> entry : pendingOrders.entrySet()) {
//            Transaction transaction = entry.getValue();
//            BuyRequestDto buyRequest = new BuyRequestDto();
//            buyRequest.setUserId(transaction.getUserId());
//            buyRequest.setBaseCurrency(transaction.getBaseCurrency());
//            buyRequest.setTargetCurrency(transaction.getTargetCurrency());
//            buyRequest.setTotal(transaction.getAmount());
//            buyRequest.setPrice(transaction.getPrice());
//            buyRequest.setTransactionTime(transaction.getTransactionTime());
//
//            try {
//                if (buyRequest.getTotal().compareTo(buyRequest.getPendingOrderBuy()) >= 0) {
//                    // 市场价格等于或低于限价，成交订单
//                    executeOrder(transaction);
//                    pendingOrders.remove(entry.getKey());
//                }
//            } catch (Exception e) {
//                // 处理异常
//                e.printStackTrace();
//            }
//        }
//    }

    //成交订单
    private void executeOrder(Transaction transaction) {
        try {
            BuyRequestDto buyRequest = new BuyRequestDto();
            buyRequest.setUserId(transaction.getUserId());
            buyRequest.setBaseCurrency(transaction.getBaseCurrency());
            buyRequest.setTargetCurrency(transaction.getTargetCurrency());
            buyRequest.setTotal(transaction.getAmount());
            buyRequest.setPrice(transaction.getPrice());
            buyRequest.setTransactionTime(transaction.getTransactionTime());

            // 计算交易成本
            // 4. 计算交易金额和手续费（示例：手续费 0.1%）
            BigDecimal transactionAmount = buyRequest.getTotal().multiply(BigDecimal.valueOf(buyRequest.getSize()));
            BigDecimal fee = transactionAmount.multiply(BigDecimal.valueOf(0.001)); // 0.1% 手续费
            BigDecimal slippageFee = transactionAmount.multiply(BigDecimal.valueOf(0.0005)); // 0.05% 滑点费
            BigDecimal totalFees = fee.add(slippageFee);
            BigDecimal baseCurrencyDeduction =  BigDecimal.valueOf(buyRequest.getSize()).add(totalFees); // 扣除的基础货币（含手续费和滑点费）
            BigDecimal targetCurrencyAddition = transactionAmount; // 获得的目标货币

            // 封装账户操作参数（TradeVo）
            TradeVo tradeVo = new TradeVo();
            tradeVo.setUserId(buyRequest.getUserId());
            tradeVo.setBaseCurrency(buyRequest.getBaseCurrency());
            tradeVo.setTargetCurrency(buyRequest.getTargetCurrency());
            tradeVo.setBaseBalance(baseCurrencyDeduction); // 基础货币扣除金额
            tradeVo.setTargetBalance(targetCurrencyAddition); // 目标货币增加金额

            // 调用Account服务，进行客户账户的更新操作
            Result<?> result = accountFeign.exchange(tradeVo);
            if(result.getCode() != 200) {
                transaction.setStatus("FAILED");
                throw new RuntimeException("账户余额更新失败");
            }


            //6、记录资金流水（funds_history 表），类型为 withdrawal，备注 “外汇买入”
            recordFundsHistory(transaction.getUserId(), tradeVo.getBaseBalance(),
                    "外汇买入"+buyRequest.getTradeType(), transaction.getTransactionId());

            // 7、更新订单状态为成交
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);

            //8、发送通知给用户
            sendTransactionNotification(transaction, buyRequest);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
    }

    protected Transaction createTransaction(BuyRequestDto buyRequest) {
        // 生成唯一的交易 ID
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId(buyRequest.getUserId());
        transaction.setBaseCurrency(buyRequest.getBaseCurrency());
        transaction.setTargetCurrency(buyRequest.getTargetCurrency());
        transaction.setAmount(buyRequest.getTotal());
        transaction.setPrice(buyRequest.getPrice());
        transaction.setTransactionTime(buyRequest.getTransactionTime());
        transaction.setAction("BUY");

        // 保存交易记录到数据库
        transactionRepository.save(transaction);
        return transaction;
    }

    //记录资金流水
    private void recordFundsHistory(String userId, BigDecimal amount, String description, String transactionId) {
        // 实际需操作 funds_history 表，示例代码（需补充具体实现）
        FundsHistory fundsHistory = new FundsHistory();
        fundsHistory.setTransactionId(transactionId);
        fundsHistory.setUserId(userId);
        fundsHistory.setAmount(amount);
        fundsHistory.setDescription(description);
        fundsHistory.setCreatedAt(new Date());
        fundsHistoryRepository.save(fundsHistory); // 假设存在仓储类
    }

    private void sendTransactionNotification(Transaction transaction, BuyRequestDto buyRequest) throws JsonProcessingException {
        // 构造 Kafka 消息
        Map<String, Object> kafkaMessage = new HashMap<>();
        kafkaMessage.put("transactionId", transaction.getTransactionId());
        kafkaMessage.put("status", transaction.getStatus());
        kafkaMessage.put("action", transaction.getAction());

        ObjectMapper objectMapper = new ObjectMapper();

        String message = objectMapper.writeValueAsString(kafkaMessage);
        // 发送到 Kafka 主题（notification-service 监听）
        kafkaTemplate.send("trade-notifications", message);

    }
}
