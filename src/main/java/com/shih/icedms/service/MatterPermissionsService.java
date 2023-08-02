package com.shih.icedms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icedms.dto.AccessRoleDto;
import com.shih.icedms.dto.MatterActionDto;
import com.shih.icedms.entity.MatterPermissions;
import com.shih.icedms.enums.ActionEnum;

import java.util.List;

/**
* @author 1
* @description 针对表【matter_permissions】的数据库操作Service
* @createDate 2023-06-04 20:47:35
*/
public interface MatterPermissionsService extends IService<MatterPermissions> {
    int getMatterPermission(String matterId,String userId);
    boolean checkMatterPermission(String matterId, ActionEnum actionEnum);
    boolean checkRoleExists(String roleId,int roleType);
    List<AccessRoleDto> accessRoleListByMatterId(String matterId);
    List<AccessRoleDto> addPermission(List<MatterActionDto> matterActionDtoList);
}
