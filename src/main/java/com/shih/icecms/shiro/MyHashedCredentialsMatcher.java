package com.shih.icecms.shiro;

import com.alibaba.fastjson.JSON;
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
    @Resource
    RedisTemplate<Object,Object> redisTemplate;

    public static final String KEY_PREFIX = "shiro:cache:retryLimit:";

    public static final Integer MAX = 5;// 最大登录次数

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
        String key = KEY_PREFIX + userName;
        // 获取用户登录失败次数
        RedisAtomicInteger atomicInteger=atomicInteger= new RedisAtomicInteger (key,redisTemplate.getConnectionFactory());
        User user = (User) info.getPrincipals().getPrimaryPrincipal();
        atomicInteger.expire(5, TimeUnit.MINUTES);
        if (atomicInteger.incrementAndGet() > MAX){
            // 如果用户登录失败次数大于5次，抛出锁定用户异常，并修改数据库用户状态字段
            if (user != null && user.getStatus() == 1){
                user.setStatus(2);// 设置为锁定状态
                userService.updateById(user);
            }
            log.info("锁定用户"+ userName);
            throw new ExcessiveAttemptsException();
        }
        // 判断用户的账号和密码是否正确
        boolean matches = BCrypt.checkpw(passWord, user.getPassword());
        // 如果匹配上了
        if (matches){
            atomicInteger.expire(0,TimeUnit.SECONDS);
        } else {
            throw new AuthenticationException("密码错误,还有"+(5-atomicInteger.get())+"次机会");
        }
        return true;
    }
}
