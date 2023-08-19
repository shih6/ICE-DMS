package com.shih.icedms.config;

import com.shih.icedms.dto.ApiResult;
import io.minio.errors.MinioException;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({AuthenticationException.class})
    public Object handle(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResult<>(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(),null));
    }
    @ExceptionHandler({RedisConnectionFailureException.class})
    public Object RedisConnectionFailureExceptionHandle(RedisConnectionFailureException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResult<>(HttpStatus.BAD_REQUEST.value(), "缓存连接失败",null));
    }
    @ExceptionHandler({RuntimeException.class, MinioException.class})
    public Object RuntimeExceptionHandle(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResult<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }
}
