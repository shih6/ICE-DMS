package com.shih.icedms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatterActionDto {
    private String matterId;
    private String roleId;
    private int roleType;
    private int actionNum;
}
