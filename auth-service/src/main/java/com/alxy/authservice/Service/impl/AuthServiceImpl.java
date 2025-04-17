package com.alxy.authservice.Service.impl;

import com.alxy.authservice.DTO.Result;
import com.alxy.authservice.Entity.User;
import com.alxy.authservice.Repository.UserRepository;
import com.alxy.authservice.Service.AuthService;
import com.alxy.authservice.utils.JwtUtil;
import com.alxy.authservice.utils.Md5Util;
import jakarta.annotation.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * 账号密码登录
     *
     * @param username 手机号
     * @param password 密码
     * @return Result<String> 包含token的登录结果
     */
    @Override
    public Result<?> loginWithPassword(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        boolean validPassword = Md5Util.checkPassword(password, user.getPassword());
        return validPassword ? getToken(user) : Result.error("密码错误");
    }

    /**
     * OAuth2登录（待实现）
     *
     * @param oauthCode OAuth2授权码
     * @param provider  OAuth2提供商
     * @return Result<String> 包含token的登录结果
     */
    @Override
    public Result<?> loginWithOAuth2(String oauthCode, String provider) {
        // TODO: 实现OAuth2登录逻辑
        // 1. 验证oauthCode
        // 2. 获取用户信息
        // 3. 创建或关联用户
        // 4. 生成token
        return Result.error("功能尚未实现");
    }


    /**
     * 生成JWT token
     *
     * @param user 用户信息
     * @return Result<String> 包含token的结果
     */
    @Override
    public Result<?> getToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("role", "user");
        String token = JwtUtil.genToken(claims);
        return Result.success(token,null);
    }
}