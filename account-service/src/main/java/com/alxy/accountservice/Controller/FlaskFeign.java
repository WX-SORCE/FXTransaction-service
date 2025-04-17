package com.alxy.accountservice.Controller;

import com.alxy.accountservice.DTO.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "flask-service", path = "/v1/flask")
public interface FlaskFeign {

    @PostMapping(value = "/getTokenByFace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<?> getTokenByFace(@RequestPart("image") MultipartFile file);

    @PostMapping("/validateFaceToken")
    boolean validateFaceToken(@RequestParam String username, @RequestParam String faceToken);
}
