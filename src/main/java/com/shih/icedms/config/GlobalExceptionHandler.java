package com.shih.icedms.config;

import com.shih.icedms.dto.ApiResult;
import org.apache.shiro.authc.AuthenticationException;
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
}
