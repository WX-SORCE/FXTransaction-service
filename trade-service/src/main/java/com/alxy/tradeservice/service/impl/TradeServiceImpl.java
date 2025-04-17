package com.alxy.tradeservice.service.impl;

import com.alxy.tradeservice.entity.Account;
import com.alxy.tradeservice.entity.Transaction;
import com.alxy.tradeservice.entity.FundsHistory;
import com.alxy.tradeservice.entity.StopLossTakeProfit;

import com.alxy.tradeservice.entity.dto.BuyRequestDto;
import com.alxy.tradeservice.entity.dto.MarginRequest;
//import com.alxy.tradeservice.feign.FeignAccount;
import com.alxy.tradeservice.entity.dto.Result;
import com.alxy.tradeservice.feign.FeignAccount;
import com.alxy.tradeservice.feign.RiskFeign;
import com.alxy.tradeservice.repository.TransactionsRepository;
import com.alxy.tradeservice.repository.FundsHistoryRepository;
import com.alxy.tradeservice.repository.StopLossTakeProfitRepository;
import com.alxy.tradeservice.utils.ThreadLocalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 外汇交易服务接口的实现类，实现了 TradeService 接口中定义的所有方法。
 * 该类负责处理外汇交易的具体业务逻辑，包括不同类型订单处理、成本计算、账户余额检查和更新、交易记录等。
 */
@Service
public class TradeServiceImpl {


    // 注入交易记录仓库，用于操作交易记录表
    @Autowired
    private TransactionsRepository transactionRepository;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate; // Kafka 消息模板

    // 注入资金流水仓库，用于操作资金流水表
    @Autowired
    private FundsHistoryRepository fundsHistoryRepository;

    // 注入止损止盈记录仓库，用于操作止损止盈记录表
    @Autowired
    private StopLossTakeProfitRepository stopLossTakeProfitRepository;

    @Autowired
    private FeignAccount accountFeign;





    @Resource
    private RiskFeign riskFeign;

    // 外汇市场标准合约每手的基础货币数量
    private static final BigDecimal CONTRACT_SIZE = new BigDecimal("100000");
    // 手续费率，固定为 0.001
    private static final BigDecimal FEE_RATE = new BigDecimal("0.001");

    @Autowired
    FeignAccount feignAccount;

    /**
     * 计算买入交易所需保证金的具体实现方法。
     * 根据手数、汇率和杠杆比例计算买入交易需要的保证金金额。
     *
     * @param lotSize       交易的手数
     * @param exchangeRate  交易时的汇率
     * @param leverageRatio 杠杆比例
     * @return 买入交易所需的保证金金额
     */

    public BigDecimal calculateBuyMargin(BigDecimal lotSize, BigDecimal exchangeRate, Long leverageRatio) {
        return lotSize.multiply(CONTRACT_SIZE).multiply(exchangeRate).divide(new BigDecimal(leverageRatio)).add(lotSize.multiply(CONTRACT_SIZE).multiply(exchangeRate).multiply(BigDecimal.valueOf(0.001)));
    }

    /**
     * 计算买入交易成本的具体实现方法。
     * 交易成本包括手续费和滑点成本，根据手数、汇率和滑点计算总成本。
     *
     * @param lotSize      交易的手数
     * @param exchangeRate 交易时的汇率
     * @param spread       滑点
     * @return 买入交易的总成本
     */

    public BigDecimal calculateBuyCost(BigDecimal lotSize, BigDecimal exchangeRate, BigDecimal spread) {
        // 计算手续费
        BigDecimal fee = lotSize.multiply(CONTRACT_SIZE).multiply(exchangeRate).multiply(FEE_RATE);
        // 计算滑点成本
        BigDecimal spreadCost = lotSize.multiply(CONTRACT_SIZE).multiply(spread);
        return fee.add(spreadCost);
    }

    /**
     * 计算卖出交易可获得金额的具体实现方法。
     * 根据手数和汇率计算卖出一定数量的货币对可获得的金额。
     *
     * @param lotSize      交易的手数
     * @param exchangeRate 交易时的汇率
     * @return 卖出交易可获得的金额
     */

    public BigDecimal calculateSellAmount(BigDecimal lotSize, BigDecimal exchangeRate) {
        return lotSize.multiply(CONTRACT_SIZE).multiply(exchangeRate);
    }

    /**
     * 计算卖出交易所需保证金的具体实现方法。
     * 根据手数、汇率和杠杆比例计算卖出交易需要的保证金金额。
     *
     * @param lotSize       交易的手数
     * @param exchangeRate  交易时的汇率
     * @param leverageRatio 杠杆比例
     * @return 卖出交易所需的保证金金额
     */

    public BigDecimal calculateSellMargin(BigDecimal lotSize, BigDecimal exchangeRate, Long leverageRatio) {
        return lotSize.multiply(CONTRACT_SIZE).multiply(exchangeRate).divide(new BigDecimal(leverageRatio));
    }

    /**
     * 计算卖出交易成本的具体实现方法。
     * 交易成本包括手续费和滑点损失，根据卖出可获得金额和滑点损失计算总成本。
     *
     * @param sellAmount 卖出交易可获得的金额
     * @param spreadLoss 滑点损失
     * @return 卖出交易的总成本
     */

    public BigDecimal calculateSellCost(BigDecimal sellAmount, BigDecimal spreadLoss) {
        // 计算手续费
        BigDecimal fee = sellAmount.multiply(FEE_RATE);
        return fee.add(spreadLoss);
    }

    /**
     * 记录资金流水信息到数据库的具体实现方法。
     * 将用户的资金变动信息记录到资金流水表中。
     *
     * @param userId      用户的唯一标识
     * @param amount      资金变动的金额
     * @param type        资金变动的类型
     * @param status      资金变动的状态
     * @param balance     变动后的账户余额
     * @param description 资金变动的描述信息
     */

    public void recordFundsHistory(String userId, BigDecimal amount, String type, String status, BigDecimal balance, String description) {
        // 生成唯一的资金流水记录 ID
        String transactionId = UUID.randomUUID().toString();
        // 创建资金流水记录对象
        FundsHistory fundsHistory = new FundsHistory();
        fundsHistory.setTransactionId(transactionId);
        fundsHistory.setUserId(userId);
        fundsHistory.setAmount(amount);
        fundsHistory.setType(type);
        fundsHistory.setStatus(status);
        fundsHistory.setBalance(balance);
        fundsHistory.setCreatedAt(new Date());
        fundsHistory.setDescription(description);
        // 保存资金流水记录到数据库
        fundsHistoryRepository.save(fundsHistory);
    }

    /**
     * 记录止损止盈信息到数据库的具体实现方法。
     * 将用户的止损止盈设置信息记录到止损止盈记录表中。
     *
     * @param userId        用户的唯一标识
     * @param transactionId 交易的唯一标识
     * @param stopLoss      止损价格
     * @param takeProfit    止盈价格
     * @param triggerTime   触发时间
     * @param triggered     是否已触发的标志
     */

    public void recordStopLossTakeProfit(String userId, String transactionId, BigDecimal stopLoss, BigDecimal takeProfit, Date triggerTime, boolean triggered) {
        // 生成唯一的止损止盈记录 ID
        String recordId = UUID.randomUUID().toString();
        // 创建止损止盈记录对象
        StopLossTakeProfit stopLossTakeProfit = new StopLossTakeProfit();
        stopLossTakeProfit.setRecordId(recordId);
        stopLossTakeProfit.setUserId(userId);
        stopLossTakeProfit.setTransactionId(transactionId);
        stopLossTakeProfit.setStopLoss(stopLoss);
        stopLossTakeProfit.setTakeProfit(takeProfit);
        stopLossTakeProfit.setTriggerTime(triggerTime);
        stopLossTakeProfit.setTriggered(triggered);
        // 保存止损止盈记录到数据库
        stopLossTakeProfitRepository.save(stopLossTakeProfit);
    }

    /**
     * 构建交易记录实体对象的辅助方法。
     * 根据交易 ID、买入请求参数、交易状态、交易金额和汇率构建交易记录实体。
     *
     * @param buyRequestDto 包含买入订单相关信息的请求对象
     * @param status        交易的状态，如 "SUCCESS" 或 "FAILED"
     * @param amount        交易的金额
     *                      交易时的汇率价格
     * @return 构建好的交易记录实体对象
     */
    private Transaction buildTransaction(BuyRequestDto buyRequestDto, String status, BigDecimal amount) {
        Transaction transaction = new Transaction();

        transaction.setUserId(buyRequestDto.getUserId());
        transaction.setBaseCurrency(buyRequestDto.getBaseCurrency());
        transaction.setTargetCurrency(buyRequestDto.getTargetCurrency());
        transaction.setAction("BUY");
        transaction.setAmount(null);
        transaction.setPrice(buyRequestDto.getPrice());
        transaction.setStatus(status);
        transaction.setStopLoss(buyRequestDto.getPendingOrderBuy());
        transaction.setTakeProfit(null);
        transaction.setTransactionTime(buyRequestDto.getTransactionTime());
        //保证金
        transaction.setMargin(amount);
        //杠杠水平
        transaction.setLeverageRatio(buyRequestDto.getLeverageRatio());
        //交易数量
        transaction.setSize(buyRequestDto.getSize());
        transaction.setTotal(buyRequestDto.getTotal());
        transaction.setTradeType(getTransactionType(buyRequestDto.getTradeType()));
        transaction.setCompletedTime(new Date());
        transaction.setTransactionType(getTransactionType(buyRequestDto.getTradeType()));
        return transaction;
    }

    /**
     * 根据交易类型代码获取交易类型字符串的辅助方法。
     * 将交易类型代码转换为对应的交易类型字符串，如 "MARKET_ORDER"、"LIMIT_ORDER" 等。
     *
     * @param tradeType 交易类型代码
     * @return 对应的交易类型字符串
     */
    private String getTransactionType(Long tradeType) {
        switch (tradeType.intValue()) {
            case 0:
                return "市价单";
            case 1:
                return "限价单";
            case 2:
                return "止盈单";
            case 3:
                return "止损单";
            default:
                return null;
        }
    }

    /**
     * @return用户所有交易
     */
    public List<Transaction> findTransaction() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        return transactionRepository.findAllByUserId(userId);
    }

    //挂单逻辑
    public Transaction pendingTransaction(BuyRequestDto buyRequestDto) {

        // 触发风控模块进行风险评估    检查保证金、持仓限额
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        buyRequestDto.setUserId(userId);
//        // 1. 风险评估（通过 Feign 调用 risk-service）
//        Result<Account> accountResult = accountFeign.getAccount(buyRequestDto.getUserId(), buyRequestDto.getBaseCurrency());
//        String accountId = accountResult.getData().getAccountId();

//        //封装调用risk-service传递的参数
//        ForeignExchange request = new ForeignExchange();
//        request.setUserId(buyRequestDto.getUserId());
//        request.setAccountId(accountId);
//        request.setAction(buyRequestDto.getAction());
//        request.setAmount(buyRequestDto.getTotal().multiply(BigDecimal.valueOf(buyRequestDto.getSize())));
//        int monitoring = riskFeign.monitoring(request);

        // 假设限价价格存储在 pendingOrderBuy 中
        BigDecimal limitPrice = buyRequestDto.getPendingOrderBuy();
        // 计算买入交易所需的保证金和利息
        BigDecimal margin = calculateBuyMargin(BigDecimal.valueOf(buyRequestDto.getSize()), buyRequestDto.getPrice(), buyRequestDto.getLeverageRatio());
        //调用用户冻结资金TODO
        MarginRequest marginRequest = new MarginRequest();
        marginRequest.setAmount(margin);
        marginRequest.setCurrency(buyRequestDto.getBaseCurrency());
        marginRequest.setUserId(userId);
//        if (feignAccount.freezeMargin(marginRequest).getCode() == 0) {
//            // 若账户余额不足，构建失败的交易记录并保存到数据库
//            Transaction failedTransaction = buildTransaction(buyRequestDto, "FAILED", margin);
//            return transactionRepository.save(failedTransaction);
//        }
        //挂单
        Transaction transaction = buildTransaction(buyRequestDto, "PENDING", margin);
        return transactionRepository.save(transaction);
    }

    //杠杆结算
    public Transaction buyOrder(Transaction transaction) throws JsonProcessingException {
        // 获取挂单唯一的交易 ID
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        transaction.setUserId(userId);
        String transactionId =transaction.getTransactionId();
        // 计算交易的手数
        BigDecimal lotSize = new BigDecimal(transaction.getSize()).divide(CONTRACT_SIZE);
        // 获取交易时价格
        BigDecimal exchangeRate = transaction.getPendingOrderBuy();
        // 获取杠杆比例
        Long leverageRatio = transaction.getLeverageRatio();
        // 获取交易动作（买入或卖出）
        String action = transaction.getAction();
        // 获取订单类型
        String tradeType = transaction.getTradeType();
        // 获取止损价格
        BigDecimal stopLoss = transaction.getStopLoss();
        // 获取止盈价格
        BigDecimal takeProfit = transaction.getTakeProfit();
        //获取保证金
        BigDecimal margin = transaction.getMargin();
        //结算买入之前的价格
        BigDecimal buyBeforAmount = calculateSellAmount(lotSize, transaction.getPrice());
        //结算买入之后的价格
        BigDecimal buyAfterAmount = calculateSellAmount(lotSize, transaction.getPendingOrderBuy());
        //判断盈亏
        if ((buyAfterAmount.subtract(buyBeforAmount)).compareTo(BigDecimal.ZERO) > 0) {
            // 假设滑点损失为 0.001
            BigDecimal spreadLoss = new BigDecimal("0.001");
            // 计算卖出交易的滑点损失
            BigDecimal cost = calculateSellCost((buyAfterAmount.subtract(buyBeforAmount)), spreadLoss);
            BigDecimal price = ((buyAfterAmount.subtract(buyBeforAmount)).subtract(cost)).divide(transaction.getPendingOrderBuy());
            //解冻保证金增加余额
//            //TODO
//            MarginRequest marginRequest = new MarginRequest();
//            marginRequest.setAmount(price.subtract(margin));
//            marginRequest.setCurrency(transaction.getBaseCurrency());
//            marginRequest.setUserId(userId);
//            feignAccount.freezeMargin(marginRequest);

            // 记录资金流水信息
            recordFundsHistory(userId, price, "deposit", "SUCCESS", BigDecimal.ZERO, "外汇卖出，杠杆交易，扣除手续费和滑点，释放保证金");
            //构建成功的交易记录并保存到数据库
            Transaction transactionOne = new Transaction();
            transactionOne.setTransactionId(transaction.getTransactionId());
            transaction.setUserId(userId);
            transaction.setBaseCurrency(transaction.getBaseCurrency());
            transaction.setTargetCurrency(transaction.getTargetCurrency());
            transaction.setAction(transaction.getAction());
            transaction.setAmount(buyAfterAmount);
            transaction.setPrice(transaction.getPendingOrderBuy());
            transaction.setStatus("SUCCESS");
            transaction.setStopLoss(null);
            transaction.setTakeProfit(price);
            transaction.setTransactionTime(new Date());
            transaction.setCompletedTime(new Date());
            transaction.setTransactionType("automated");
            transactionRepository.save(transaction);
            // 记录止损止盈信息
            recordStopLossTakeProfit(userId, transactionId, stopLoss, price, new Date(), true);
            sendTransactionNotification(transaction);
        } else {
            // 假设滑点损失为 0.001
            BigDecimal spreadLoss = new BigDecimal("0.001");
            // 计算卖出交易的滑点损失
            BigDecimal cost = calculateSellCost((buyAfterAmount.subtract(buyBeforAmount)), spreadLoss);

            BigDecimal price = ((buyAfterAmount.subtract(buyBeforAmount)).add(cost)).divide(transaction.getPendingOrderBuy());

//            //解冻保证金减少余额
////            //TODO
//            MarginRequest marginRequest = new MarginRequest();
//            marginRequest.setAmount(price.add(margin).negate());
//            marginRequest.setCurrency(transaction.getBaseCurrency());
//            marginRequest.setUserId(userId);

//            if (feignAccount.freezeMargin(marginRequest).getCode() == 0) {
//                // 记录资金流水信息
                recordFundsHistory(userId, price, " withdrawal", "SUCCESS", BigDecimal.ZERO, "外汇卖出，杠杆交易，扣除手续费和滑点，扣除保证金");
//            } else {
                // 记录资金流水信息失败
//                recordFundsHistory(userId, price, " withdrawal", "FAILED", BigDecimal.ZERO, "外汇卖出，杠杆交易，扣除手续费和滑点，扣除保证金 ，余额不足");
//            }
            //构建成功的交易记录并保存到数据库
            Transaction transactionOne = new Transaction();
            transactionOne.setTransactionId(transaction.getTransactionId());
            transaction.setUserId(userId);
            transaction.setBaseCurrency(transaction.getBaseCurrency());
            transaction.setTargetCurrency(transaction.getTargetCurrency());
            transaction.setAction(transaction.getAction());
            transaction.setAmount(buyAfterAmount);
            transaction.setPrice(transaction.getPendingOrderBuy());
            transaction.setStatus("SUCCESS");
            transaction.setStopLoss(price);
            transaction.setTakeProfit(null);
            transaction.setTransactionTime(new Date());
            transaction.setCompletedTime(new Date());
            transaction.setTransactionType("automated");
            transactionRepository.save(transaction);

            recordStopLossTakeProfit(userId, transactionId, price, takeProfit, new Date(), true);
            sendTransactionNotification(transaction);
        }
        return transaction;
    }

    public List<FundsHistory> findFundsHistory() {Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        return fundsHistoryRepository.findByUserId(userId);
    }

    public List<StopLossTakeProfit> findStopLosssTakeProfit() {Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        return stopLossTakeProfitRepository.findByUserId(userId);
    }

    private void sendTransactionNotification(Transaction transaction) throws JsonProcessingException {
        // 构造 Kafka 消息
        Map<String, Object> kafkaMessage = new HashMap<>();
        kafkaMessage.put("transactionId", transaction.getTransactionId());
        kafkaMessage.put("status", transaction.getStatus());
        kafkaMessage.put("action", "BUY");

        ObjectMapper objectMapper = new ObjectMapper();

        String message = objectMapper.writeValueAsString(kafkaMessage);
        // 发送到 Kafka 主题（notification-service 监听）
        kafkaTemplate.send("trade-notifications", message);

    }

}