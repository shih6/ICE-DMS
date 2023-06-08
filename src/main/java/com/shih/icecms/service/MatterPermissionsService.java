package com.shih.icecms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icecms.entity.MatterPermissions;
import com.shih.icecms.enums.ActionEnum;

/**
* @author 1
* @description 针对表【matter_permissions】的数据库操作Service
* @createDate 2023-06-04 20:47:35
*/
public interface MatterPermissionsService extends IService<MatterPermissions> {
    int getMatterPermission(String matterId,String userId);
    boolean checkMatterPermission(String matterId, ActionEnum actionEnum);

    boolean checkRoleExists(String roleId,int roleType);
}
