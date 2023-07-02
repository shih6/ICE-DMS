package com.shih.icecms.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<MatterDTO> subMatters;
    public MatterDTO findNode( String targetMatterId){
        if(getId().equals(targetMatterId)){
            return this;
        }
        for (MatterDTO sub: getSubMatters()) {
            MatterDTO tmp= sub.findNode(targetMatterId);
            if(tmp!=null){
                return tmp;
            }
        }
        return null;
    }
}
