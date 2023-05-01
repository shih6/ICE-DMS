package com.shih.icecms.controller;

import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Api(value = "登录",tags = {"登录"})
public class LoginController {
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private MinioConfig prop;
    @Resource
    HttpServletRequest request;

    @ApiOperation("账号密码登录")
    @GetMapping(value = "/login")
    public ApiResult passwordLogin() {
        return ApiResult.SUCCESS("1");
    }

    @PostMapping("/upload")
    public ResponseEntity upload(@RequestParam(value = "file") MultipartFile multipartFile) throws Exception {
        minioUtil.upload(multipartFile);
        return ResponseEntity.ok().build();
    }
}