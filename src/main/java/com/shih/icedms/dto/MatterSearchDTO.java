package com.shih.icedms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatterSearchDTO {
    private String name;
    private int current;
    private int pageSize;
}
