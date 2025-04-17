package com.alxy.accountservice.Controller;

import com.alxy.accountservice.DTO.*;
import com.alxy.accountservice.Entity.Account;
import com.alxy.accountservice.Service.AccountService;
import com.alxy.accountservice.Utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/account")
public class AccountController {

    @Resource
    private AccountService accountService;
    @Resource
    private FlaskFeign flaskFeign;


    // 账户列表展示
    @GetMapping("/getAccountList")
    Result<List<Account>> getAccountList() {
        // 从ThreadLocal获取userId
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims.get("userId").toString();
        // 查询用户多个币种账户
        List<Account> accounts = accountService.getAccountByUserId(userId);
        return Result.success(accounts);
    }



    // 获取账户
    @GetMapping("/getAccount")
    Result<?> getAccount(@RequestParam String userId,@RequestParam String baseCurrency){
        Account account = accountService.getAccountByUserIdAndBaseCurrency(userId, baseCurrency);
        return Result.success(account);
    }

    // 账户充值
    @PostMapping("/recharge")
    public Result<Account> recharge(@RequestBody Recharge recharge) {
        if (recharge == null
                || recharge.getBaseCurrency() == null
                || recharge.getAmount() == null
                || recharge.getFaceToken() == null) {
            return Result.error(400, "参数不能为空");
        }

        Map<String, Object> claims = ThreadLocalUtil.get();
        String username = claims.get("username").toString();
        String userId = claims.get("userId").toString();

        try {
            // 调用人脸 token 验证
            boolean tokenResult = flaskFeign.validateFaceToken(username, recharge.getFaceToken());
            if (!tokenResult) {
                return Result.error(401, "人脸Token验证失败");
            }
            // 验证通过，执行充值
            return accountService.recharge(recharge.getBaseCurrency(), userId, recharge.getAmount());
        } catch (Exception e) {
            return Result.error(500, "充值异常：" + e.getMessage());
        }
    }


    // 兑换单 base-target
    @PostMapping("/exchange")
    public Result<?> exchange(@RequestBody TradeVo tradeVo) {
        // 查找基础币种账户
        Account baseAccount = accountService.getAccountByUserIdAndBaseCurrency(tradeVo.getUserId(), tradeVo.getBaseCurrency());
        if (baseAccount == null) {
            return Result.error("基础币种账户不存在");
        }
        // 查找目标币种账户
        Account targetAccount = accountService.getAccountByUserIdAndBaseCurrency(tradeVo.getUserId(), tradeVo.getTargetCurrency());
        if (targetAccount == null) {
            return Result.error("目标币种账户不存在");
        }
        // 判断余额是否足够
        if (baseAccount.getBalance().compareTo(tradeVo.getBaseBalance()) < 0) {
            return Result.error("基础币种账户余额不足");
        }
        // 扣减余额
        baseAccount.setBalance(baseAccount.getBalance().subtract(tradeVo.getBaseBalance()));
        boolean baseUpdateSuccess = accountService.updateAccount(baseAccount);
        if (!baseUpdateSuccess) {
            return Result.error("基础币种账户余额更新失败");
        }
        // 增加目标币种账户余额
        targetAccount.setBalance(targetAccount.getBalance().add(tradeVo.getTargetBalance()));
        boolean targetUpdateSuccess = accountService.updateAccount(targetAccount);
        if (!targetUpdateSuccess) {
            return Result.error("目标币种账户余额更新失败");
        }
        // 如果都成功
        return Result.success();
    }


    @PostMapping("/freezeMargin")
    public Result<?> freezeMargin(@RequestBody MarginRequest request) {
        Account account = accountService.getAccountByUserIdAndBaseCurrency(request.getUserId(), request.getCurrency());
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            return Result.error("余额不足");
        }
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        account.setMarginBalance(account.getMarginBalance().add(request.getAmount()));
        accountService.updateAccount(account);
        return Result.success();
    }

    @PostMapping("/releaseMargin")
    public Result<?> releaseMargin(@RequestBody MarginRequest request) {
        Account account = accountService.getAccountByUserIdAndBaseCurrency(request.getUserId(), request.getCurrency());
        if (account.getMarginBalance().compareTo(request.getAmount()) < 0) {
            return Result.error("保证金不足");
        }
        account.setMarginBalance(account.getMarginBalance().subtract(request.getAmount()));
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountService.updateAccount(account);
        return Result.success();
    }

    @PostMapping("/settleTrade")
    public Result<?> settleTrade(@RequestBody TradeSettleRequest req) {
        Account base = accountService.getAccountByUserIdAndBaseCurrency(req.getUserId(), req.getBaseCurrency());
        Account target = accountService.getAccountByUserIdAndBaseCurrency(req.getUserId(), req.getTargetCurrency());
        if (base.getMarginBalance().compareTo(req.getBaseAmount()) < 0) {
            return Result.error("保证金不足");
        }
        base.setMarginBalance(base.getMarginBalance().subtract(req.getBaseAmount()));
        target.setBalance(target.getBalance().add(req.getTargetAmount()));
        accountService.updateAccount(base);
        accountService.updateAccount(target);
        return Result.success();
    }

    @PostMapping("/leverageTrade")
    public Result<?> leverageTrade(@RequestBody TradeRequest req) {
        Account base = accountService.getAccountByUserIdAndBaseCurrency(req.getUserId(), req.getBaseCurrency());
        Account target = accountService.getAccountByUserIdAndBaseCurrency(req.getUserId(), req.getTargetCurrency());

        if ("OPEN".equalsIgnoreCase(req.getAction())) {
            if (base.getBalance().compareTo(req.getMarginAmount()) < 0) {
                return Result.error("余额不足，无法开仓");
            }
            base.setBalance(base.getBalance().subtract(req.getMarginAmount()));
            base.setMarginBalance(base.getMarginBalance().add(req.getMarginAmount()));
            accountService.updateAccount(base);
            return Result.success("杠杆开仓成功");
        }
        if ("CLOSE".equalsIgnoreCase(req.getAction())) {
            if (base.getMarginBalance().compareTo(req.getMarginAmount()) < 0) {
                return Result.error("保证金不足，无法平仓");
            }
            base.setMarginBalance(base.getMarginBalance().subtract(req.getMarginAmount()));
            target.setBalance(target.getBalance().add(req.getTargetProfit()));
            accountService.updateAccount(base);
            accountService.updateAccount(target);
            return Result.success("杠杆平仓成功");
        }

        return Result.error("无效操作");
    }

}