package com.shih.icecms.service;

import com.shih.icecms.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 1
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2023-06-04 18:23:32
*/
public interface UsersService extends IService<Users> {
    String getRId(String authCode);
}
