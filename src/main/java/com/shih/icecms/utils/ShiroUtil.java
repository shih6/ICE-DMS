package com.shih.icecms.utils;

import com.shih.icecms.entity.Users;
import com.shih.icecms.service.UsersService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ShiroUtil {
    @Resource
    private UsersService usersService;
    public Users getLoginUser(){
        return usersService.getById("test");
        /*Object obj=SecurityUtils.getSubject().getPrincipal();
        if(obj==null){
            return usersService.getById("test");
        }else{
            return (Users)obj;
        }*/

    }
}
