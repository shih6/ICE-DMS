package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.entity.Users;
import com.shih.icecms.service.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(value = "登录",tags = {"登录"})
public class UserController {
    @Resource
    HttpServletRequest request;
    @Resource
    UsersService usersService;

    @ApiOperation("账号密码登录")
    @GetMapping(value = "/login")
    public ApiResult passwordLogin(@RequestParam String userName,@RequestParam String passWord) {
        Users users=usersService.getOne(new QueryWrapper<Users>().lambda().eq(Users::getUsername,userName));
        if(users!=null&&users.getPassword().equals(passWord)){
            return ApiResult.SUCCESS(users);
        }else{
            return ApiResult.ERROR("账号密码错误");
        }
    }

    @ApiOperation("钉钉鉴权登录V2")
    @GetMapping( value = "/login/auth")
    public ApiResult qrLogin(@ApiParam("钉钉给的AuthCode")@RequestParam(value = "auth_code")String authCode) {
        String rId=usersService.getRId(authCode);
        Users users=usersService.getOne(new QueryWrapper<Users>().lambda().eq(Users::getDingtalkId,rId));
        if(users!=null){
            return ApiResult.SUCCESS(users);
        }else{
            return ApiResult.ERROR("未绑定账号");
        }
    }
    @ApiOperation("获取用户列表")
    @GetMapping("/user/list")
    public ApiResult<List<Users>> list(){
        List<Users> usersList=usersService.list(new QueryWrapper<Users>().lambda().eq(Users::getStatus,1));
        return ApiResult.SUCCESS(usersList);
    }

}