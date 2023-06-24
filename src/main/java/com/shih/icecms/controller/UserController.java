package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.CommonUtil;
import com.shih.icecms.utils.JwtUtil;
import com.shih.icecms.utils.MinioUtil;
import com.shih.icecms.utils.ShiroUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@RestController
@Api(value = "登录",tags = {"登录"})
@Slf4j
public class UserController {
    @Resource
    HttpServletRequest request;
    @Resource
    HttpServletResponse response;
    @Resource
    UsersService usersService;
    @Autowired
    private ShiroUtil shiroUtil;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private MatterService matterService;

    @ApiOperation("账号密码登录")
    @GetMapping(value = "/user/login")
    public ApiResult passwordLogin(@RequestParam String userName,@RequestParam String passWord) {
        User user =usersService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername,userName));
        if(user !=null&& BCrypt.checkpw(passWord, user.getPassword())){
            response.setHeader("Authorization", JwtUtil.createJWT(user.getUsername(),passWord));
            return ApiResult.SUCCESS(user);
        }else{
            return ApiResult.ERROR("账号或密码错误");
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
            return ApiResult.ERROR("未绑定账号，请使用账号密码登录");
        }
    }
    @ApiOperation("绑定账号")
    @GetMapping( value = "/user/bindDingTalkAccount")
    public ApiResult bindDingTalkAccount(@ApiParam("钉钉给的AuthCode")@RequestParam(value = "auth_code")String authCode) {
        String rId=usersService.getRId(authCode);
        User tmp=usersService.getOne(new LambdaQueryWrapper<User>().eq(User::getDingtalkId,rId));
        if (tmp!=null) {
            return ApiResult.ERROR("此账号已绑定至："+tmp.getUsername());
        }
        User user = shiroUtil.getLoginUser();
        user.setDingtalkId(rId);
        usersService.updateById(user);
        return ApiResult.SUCCESS(user);
    }
    @ApiOperation("获取用户列表")
    @GetMapping("/user/list")
    public ApiResult<List<User>> list(@RequestParam(required = false,defaultValue = "1") Integer status){
        User user =shiroUtil.getLoginUser();
        List<User> userList =usersService.list(new QueryWrapper<User>().lambda().eq(status!=-1,User::getStatus,status));
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
        Matter matter=new Matter();
        matter.setId(userDto.getId());
        matter.setCreator(userDto.getId());
        matter.setType(0);
        matter.setName(matter.getId());
        matter.setParentId("private");
        matter.setCreateTime(new Date().getTime());
        matter.setStatus(1);
        matter.setModifiedTime(new Date().getTime());
        matterService.save(matter);
        return ApiResult.SUCCESS(userDto);
    }
    @ApiOperation("上传头像")
    @PostMapping("/avatar/upload")
    public ApiResult uploadAvatar(@RequestParam(value = "file") MultipartFile multipartFile){
        User user=shiroUtil.getLoginUser();
        if(org.apache.shiro.util.StringUtils.hasText(user.getAvatar())){
            try {
                minioUtil.delete(user.getAvatar());
            } catch (MinioException | RuntimeException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                log.error(e.getMessage());
            }
        }
        String prefix="avatar/";
        String objectName=user.getId()+ CommonUtil.getFilenameExtensionWithDot(multipartFile.getOriginalFilename());
        minioUtil.upload(multipartFile, prefix+objectName);
        user.setAvatar(objectName);
        usersService.updateById(user);
        return ApiResult.SUCCESS(objectName);
    }
    @ApiOperation("下载头像")
    @GetMapping("/avatar/{objectName}")
    public void downloadAvatar(@PathVariable("objectName") String objectName) throws MinioException, IOException {
        String prefix="avatar/";
        minioUtil.download(prefix+objectName, response);
    }
}