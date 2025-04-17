package com.alxy.authservice.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Request {
    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String Name;

    private String email;

}