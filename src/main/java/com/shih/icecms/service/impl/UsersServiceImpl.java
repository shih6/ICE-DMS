package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.entity.Users;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2023-05-01 19:29:55
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




