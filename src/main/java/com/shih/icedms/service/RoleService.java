package com.shih.icedms.service;

import com.shih.icedms.dto.RoleDto;
import com.shih.icedms.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 1
* @description 针对表【role】的数据库操作Service
* @createDate 2023-06-06 22:41:31
*/
public interface RoleService extends IService<Role> {
    boolean hasRole(String roleId);
    List<RoleDto> roleList();
}
