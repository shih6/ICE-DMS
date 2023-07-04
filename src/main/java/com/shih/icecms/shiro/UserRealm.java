package com.shih.icecms.shiro;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Service
public class UserRealm extends AuthorizingRealm {
    @Autowired
    private UsersService usersService;
    @Autowired
    private HttpServletResponse response;
    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }
    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) {
        String token = (String) auth.getCredentials();
        try {
            if(JwtUtil.isExpire(token)){
                throw new AuthenticationException("token过期，请重新登入！");
            }
            // 解密获得username，用于和数据库进行对比
            HashMap<String,String> json= JSON.parseObject(JwtUtil.parseJWT(token).getSubject(), HashMap.class);
            String userName = json.get("userName");
            String passWord = json.get("passWord");
            User user = usersService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userName));
            if(user ==null){
                throw new AuthenticationException("账号不存在");
            }
            if (userName == null) {
                throw new AuthenticationException("token错误，请重新登入！");
            }

            return new SimpleAuthenticationInfo(user, passWord, getName());
        } catch (JwtException e) {
            throw new AuthenticationException("token错误，请重新登入！");
        }
    }


}
