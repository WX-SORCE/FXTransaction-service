package com.alxy.authservice.Service;

import com.alxy.authservice.DTO.Result;
import com.alxy.authservice.Entity.User;

public interface AuthService {


    public Result<?> loginWithPassword(String username, String password);

    public Result<?> loginWithOAuth2(String oauthCode, String provider);

    Result<?> getToken(User user);
}