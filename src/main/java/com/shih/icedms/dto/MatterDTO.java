package com.shih.icedms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

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
    private Long size;
    private Boolean extendSuper;
    // 新增字段
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatterDTO matterDTO = (MatterDTO) o;
        return Objects.equals(id, matterDTO.id) && Objects.equals(parentId, matterDTO.parentId) && Objects.equals(name, matterDTO.name) && Objects.equals(type, matterDTO.type) && Objects.equals(createTime, matterDTO.createTime) && Objects.equals(modifiedTime, matterDTO.modifiedTime) && Objects.equals(creator, matterDTO.creator) && Objects.equals(status, matterDTO.status) && Objects.equals(size, matterDTO.size) && Objects.equals(extendSuper, matterDTO.extendSuper) && Objects.equals(action, matterDTO.action) && Objects.equals(creatorName, matterDTO.creatorName) && Objects.equals(subMatters, matterDTO.subMatters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, name, type, createTime, modifiedTime, creator, status, size, extendSuper, action, creatorName, subMatters);
    }
}
