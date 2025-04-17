package com.alxy.authservice.Controller;


import com.alxy.authservice.DTO.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "flask-service", path = "/v1/flask")
public interface FlaskFeign {

    @PostMapping(value = "/faceIdentity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<?> faceIdentity(@RequestPart("image") MultipartFile image);

}
