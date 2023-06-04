package com.shih.icecms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken {
    private Integer id;
    private String accessToken;
    private Integer expiresIn;
    private LocalDateTime createTime;
}
