package com.shih.icecms.utils;

import com.shih.icecms.entity.User;
import com.shih.icecms.service.UsersService;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ShiroUtil {
    @Resource
    private UsersService usersService;
    public User getLoginUser()  {
        return (User)SecurityUtils.getSubject().getPrincipal();
    }
}
