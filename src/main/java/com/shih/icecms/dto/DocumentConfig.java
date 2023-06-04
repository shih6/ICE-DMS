package com.shih.icecms.dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentConfig {
    private String title;
    private String url;
    private String key;
    private List<History> histories;
}
