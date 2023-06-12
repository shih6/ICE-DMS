package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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
    public ApiResult<List<User>> list(@RequestParam(required = false,defaultValue = "1") String status){
        User user =shiroUtil.getLoginUser();
        List<User> userList =usersService.list(new QueryWrapper<User>().lambda().eq(!status.equals("-1"),User::getStatus,status));
        return ApiResult.SUCCESS(userList);
    }
    @ApiOperation("账号状态修改")
    @PostMapping("/user/status")
    public ApiResult<User> userStatus(@RequestBody User userDto){
        usersService.updateById(userDto);
        return ApiResult.SUCCESS(userDto);
    }
    @ApiOperation("创建账号")
    @PostMapping("/user/add")
    public ApiResult userAdd(@RequestBody User userDto){
        if(!StringUtils.hasText(userDto.getUsername())){
            return ApiResult.ERROR("用户名不能为空");
        }
        if(!StringUtils.hasText(userDto.getActualName())){
            return ApiResult.ERROR("姓名不能为空");
        }
        if(!StringUtils.hasText(userDto.getPassword())){
            return ApiResult.ERROR("密码不能为空");
        }

        if(usersService.count(new LambdaQueryWrapper<User>().eq(User::getUsername,userDto.getUsername()))>0){
            return ApiResult.ERROR("用户名不能重复");
        }
        usersService.save(userDto);
        return ApiResult.SUCCESS(userDto);
    }
}