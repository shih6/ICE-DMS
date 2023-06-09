package com.shih.icecms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatterDTO {
    private String id;
    private String parentId;
    private String name;
    private Integer type;
    private Long createTime;
    private Long modifiedTime;
    private String creator;
    private Integer status;
    // 新增字段
    private Integer size;
    private Integer action;
    private String creatorName;
}
