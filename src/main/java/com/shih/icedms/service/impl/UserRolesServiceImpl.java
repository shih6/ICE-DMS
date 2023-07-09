package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.entity.UserRoles;
import com.shih.icedms.service.UserRolesService;
import com.shih.icedms.mapper.UserRolesMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【user_roles】的数据库操作Service实现
* @createDate 2023-05-07 20:15:10
*/
@Service
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles>
    implements UserRolesService{

}




