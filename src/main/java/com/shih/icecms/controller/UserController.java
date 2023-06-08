package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.JwtUtil;
import com.shih.icecms.utils.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(value = "登录",tags = {"登录"})
public class UserController {
    @Resource
    HttpServletRequest request;
    @Resource
    HttpServletResponse response;
    @Resource
    UsersService usersService;
    @Autowired
    private ShiroUtil shiroUtil;

    @ApiOperation("账号密码登录")
    @GetMapping(value = "/user/login")
    public ApiResult passwordLogin(@RequestParam String userName,@RequestParam String passWord) {
        User user =usersService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername,userName));
        if(user !=null&& BCrypt.checkpw(passWord, user.getPassword())){
            response.setHeader("Authorization", JwtUtil.createJWT(user.getUsername(),passWord));
            return ApiResult.SUCCESS(user);
        }else{
            return ApiResult.ERROR("账号密码错误");
        }
    }

    @ApiOperation("钉钉鉴权登录V2")
    @GetMapping( value = "/login/auth")
    public ApiResult qrLogin(@ApiParam("钉钉给的AuthCode")@RequestParam(value = "auth_code")String authCode) {
        String rId=usersService.getRId(authCode);
        User user =usersService.getOne(new QueryWrapper<User>().lambda().eq(User::getDingtalkId,rId));
        if(user !=null){
            return ApiResult.SUCCESS(user);
        }else{
            return ApiResult.ERROR("未绑定账号");
        }
    }
    @ApiOperation("获取用户列表")
    @GetMapping("/user/list")
    public ApiResult<List<User>> list(){
        User user =shiroUtil.getLoginUser();
        List<User> userList =usersService.list(new QueryWrapper<User>().lambda().eq(User::getStatus,1));
        return ApiResult.SUCCESS(userList);
    }

}