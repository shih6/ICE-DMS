package com.shih.icecms.shiro;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MyHashedCredentialsMatcher extends HashedCredentialsMatcher {

    @Resource
    private UsersService userService;//从数据库里获取用户信息的service
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // 获取用户名
        HashMap<String,String> json= null;
        try {
            json = JSON.parseObject(JwtUtil.parseJWT(token.getCredentials().toString()).getSubject(), HashMap.class);
        } catch (Exception e) {
            throw new AuthenticationException("token过期，请重新登入！");
        }
        String userName = json.get("userName");
        String passWord = json.get("passWord");
        // 获取用户登录失败次数
        User user = userService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername,userName));
        if(user.getStatus()==2){
            throw new AuthenticationException("账号已被锁定");
        }
        // 判断用户的账号和密码是否正确
        if(!passWord.equals("qrlogin")&&!BCrypt.checkpw(passWord, user.getPassword())){
            throw new AuthenticationException("密码或用户名已更改，请重新登录");
        }
        return true;
    }
}
