package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.entity.Role;
import com.shih.icecms.service.RoleService;
import com.shih.icecms.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【role】的数据库操作Service实现
* @createDate 2023-06-06 22:41:31
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




