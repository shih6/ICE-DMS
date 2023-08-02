package com.shih.icedms.dto;

import com.shih.icedms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessRoleDto {

    private String permissionId;

    private String matterId;

    private String roleId;

    private Integer roleType;

    private Integer action;

    private String roleName;

    private String roleDesc;

    private String avatar;

    private List<User> userList;
}
