package com.alxy.authservice.Controller;


import com.alxy.authservice.DTO.Account;
import com.alxy.authservice.DTO.Result;
import com.alxy.authservice.Entity.User;
import com.alxy.authservice.Repository.UserRepository;
import com.alxy.authservice.Service.AuthService;
import com.alxy.authservice.utils.MailUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {
    @Resource
    private AuthService authService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private FlaskFeign flaskFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private MailUtil mailUtil;

    @PostMapping("/pwdLogin")
    public Result<?> loginWithPassword(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        Result<?> result = authService.loginWithPassword(username, password);
        if (result.getCode() == 200) {
            return Result.success(result.getToken(), null);
        }
        return Result.error("登录失败");
    }

    @GetMapping("/getUser")
    public Result<User> getUser(String userId) {
        User user = userRepository.findUserByUserId(userId);
        return Result.success(user);
    }

    @PostMapping("/faceLogin")
    public Result<?> faceLogin(@RequestParam("image") MultipartFile file) {
        String username = flaskFeign.faceIdentity(file).getData().toString();
        System.out.println("############################"+username);
        User user = userRepository.findUserByUsername(username);
        Result<?> result = authService.getToken(user);
        if (result.getCode() == 200) {
            return Result.success(result.getToken(),null);
        }
        return Result.error("人脸识别失败");
    }

    // 发送验证码 （邮箱验证用）
    @PostMapping("/sendValidation")
    public Result<?> sendValidation(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        User user = userRepository.getUserByEmail(email);
        return user != null ? mailUtil.SendValidation(email) : Result.error("此邮箱未绑定或者未注册");
    }

    // 邮箱登录
    @PostMapping("/emailLogin")
    public Result<?> emailLogin(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        ValueOperations<String, String> operator = redisTemplate.opsForValue();
        User user = userRepository.getUserByEmail(email);
        if (user == null) return Result.error("邮箱未验证或绑定");
        boolean validCode = code.equals(operator.get(email));
        return validCode ? authService.getToken(user) : Result.error("验证码错误");
    }

}