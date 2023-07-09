package com.shih.icedms.mapper;

import com.shih.icedms.dto.AccessRoleDto;
import com.shih.icedms.entity.MatterPermissions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 1
* @description 针对表【matter_permissions】的数据库操作Mapper
* @createDate 2023-06-04 20:47:35
* @Entity com.shih.icedms.entity.MatterPermissions
*/
public interface MatterPermissionsMapper extends BaseMapper<MatterPermissions> {
    List<AccessRoleDto> accessRoleListByMatterId(String matterId);
}




