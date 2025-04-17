package com.xupt.flask.Controller;

import com.xupt.flask.DTO.Result;
import com.xupt.flask.Service.FlaskService;
import com.xupt.flask.Utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/v1/flask")
public class FlaskController {
    @Resource
    private FlaskService flaskService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 人脸登录
    @PostMapping("/faceIdentity")
    public Result<?> FaceIdentity(@RequestParam("image") MultipartFile file) throws Exception {
        String username = flaskService.Face2User(file);
        return Result.success(username);
    }

    // 人脸获取token
    @PostMapping("/getTokenByFace")
    public Result<?> getTokenByFace(@RequestParam("image") MultipartFile file) throws Exception {
        String username = flaskService.Face2User(file);
        if (Objects.equals(username, "unknown") || username.isEmpty()) {
            return Result.error("人脸识别失败");
        }
        String token = createToken(username);
        String redisKey = "face_token:" + username;
        // 存入redis，5分钟过期
        redisTemplate.opsForValue().set(redisKey, token, Duration.ofMinutes(5));
        return Result.success(token);
    }

    @GetMapping("/forecast")
    public Result<?> getForecastCurrency(@RequestParam String baseCurrency, @RequestParam String targetCurrency){
        Result<?> result = flaskService.PredictExchange(baseCurrency, targetCurrency);
        if (result.getCode() == 200) {
            return Result.success(result.getData());
        }
        return Result.error("服务出错！");
    }

    // 验证token方法
    @PostMapping("/validateFaceToken")
    public boolean validateFaceToken(String username, String faceToken) {
        String redisKey = "face_token:" + username;
        String tokenInRedis = redisTemplate.opsForValue().get(redisKey);

        // 校验成功再删除
        if (faceToken != null && faceToken.equals(tokenInRedis)) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }


    // 生成JWT token
    public String createToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return JwtUtil.genToken(claims);
    }
}

