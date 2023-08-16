package com.shih.icedms.dto;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class CreateDto {
    private String fileName;
    private String fileType;
    private String parentMatterId;
}
