package com.shih.icedms.dto;

import com.shih.icedms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    private Integer id;

    private String roleName;

    private String roleDesc;

    private String creator;

    private Date createTime;

    private List<User> users;
}
