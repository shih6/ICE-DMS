package com.shih.icedms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.entity.Matter;
import com.shih.icedms.entity.User;
import com.shih.icedms.entity.UserRoles;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.service.UserRolesService;
import com.shih.icedms.service.UsersService;
import com.shih.icedms.utils.CommonUtil;
import com.shih.icedms.utils.JwtUtil;
import com.shih.icedms.utils.MinioUtil;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private MatterService matterService;
    @Resource
    private UserRolesService userRolesService;
    @Resource
    RedisTemplate<Object,Object> redisTemplate;
    public static final String KEY_PREFIX = "shiro:cache:retryLimit:";
    public static final Integer MAX = 5;// 最大登录次数

    @ApiOperation("账号密码登录")
    @GetMapping(value = "/user/login")
    public ApiResult passwordLogin(@RequestParam String userName,@RequestParam String passWord) {
        User user =usersService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername,userName));
        String key = KEY_PREFIX + userName;
        RedisAtomicInteger atomicInteger= new RedisAtomicInteger (key,redisTemplate.getConnectionFactory());
        atomicInteger.expire(5, TimeUnit.MINUTES);
        if (atomicInteger.incrementAndGet() > MAX){
            // 如果用户登录失败次数大于5次，抛出锁定用户异常，并修改数据库用户状态字段
            if (user != null && user.getStatus() == 1){
                user.setStatus(2);// 设置为锁定状态
                usersService.updateById(user);
                log.info("锁定用户"+ userName);
                return ApiResult.ERROR("账号已被锁定");
            }
        }
        if(user!=null&&user.getStatus()==2){
            return ApiResult.ERROR("账号已被锁定");
        }
        try{
            if(user !=null&& BCrypt.checkpw(passWord, user.getPassword())){
                atomicInteger.expire(0,TimeUnit.SECONDS);
                response.setHeader("Authorization", JwtUtil.createJWT(user.getUsername(),passWord));
                return ApiResult.SUCCESS(user);
            }
        }catch (Exception e){

        }
        return ApiResult.ERROR("账号或密码错误,还有"+(5-atomicInteger.get())+"次机会");
    }

    @ApiOperation("钉钉鉴权登录V2")
    @GetMapping( value = "/user/dingtalkAuth")
    public ApiResult qrLogin(@ApiParam("钉钉给的AuthCode")@RequestParam(value = "auth_code")String authCode) {
        String rId=usersService.getRId(authCode);
        User user =usersService.getOne(new QueryWrapper<User>().lambda().eq(User::getDingtalkId,rId));
        if(user !=null){
            response.setHeader("Authorization", JwtUtil.createJWT(user.getUsername(),"qrlogin"));
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
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        user.setDingtalkId(rId);
        usersService.updateById(user);
        return ApiResult.SUCCESS(user);
    }
    @ApiOperation("获取用户列表")
    @GetMapping("/user/list")
    public ApiResult<List<User>> list(@RequestParam(required = false,defaultValue = "1") Integer status){
        User user =(User)SecurityUtils.getSubject().getPrincipal();
        List<User> userList =usersService.list(new QueryWrapper<User>().lambda().eq(status!=-1,User::getStatus,status).orderBy(true,false,User::getIsAdmin));
        return ApiResult.SUCCESS(userList.stream().filter(p->!p.getId().equals(user.getId())).collect(Collectors.toList()));
    }
    @ApiOperation("账号状态修改")
    @PostMapping("/user/status")
    public ApiResult userStatus(@RequestBody User userDto){
        User user =(User)SecurityUtils.getSubject().getPrincipal();
        User one = usersService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userDto.getUsername()));
        if(user.getIsAdmin()!=1){
            if(!one.getId().equals(user.getId())){
                return ApiResult.ERROR("权限不足");
            }
        }

        if(one!=null&&!one.getId().equals(userDto.getId())){
            return ApiResult.ERROR("用户名不能重复");
        }
        usersService.updateById(userDto);
        Matter matter=matterService.getById(userDto.getId());
        matter.setName(userDto.getActualName());
        matterService.updateById(matter);
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
        matter.setName(userDto.getActualName());
        matter.setParentId("private");
        matter.setCreateTime(new Date().getTime());
        matter.setStatus(1);
        matter.setModifiedTime(new Date().getTime());
        matter.setExtendSuper(false);
        matterService.save(matter);
        UserRoles userRoles=new UserRoles();
        userRoles.setUserId(userDto.getId());
        userRoles.setRoleId(0);
        userRolesService.save(userRoles);
        return ApiResult.SUCCESS(userDto);
    }
    @ApiOperation("上传头像")
    @PostMapping("/avatar/upload")
    public ApiResult uploadAvatar(@RequestParam(value = "file") MultipartFile multipartFile) throws Exception {
        final String prefix="avatar/";
        User user=(User)SecurityUtils.getSubject().getPrincipal();
        if(org.apache.shiro.util.StringUtils.hasText(user.getAvatar())){
            try {
                minioUtil.delete(prefix+user.getAvatar());
            } catch (MinioException | RuntimeException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("删除头像失败 objectName"+prefix+user.getAvatar()+"  "+e.getMessage());
            }
        }
        String avatarName=user.getId()+ CommonUtil.getFilenameExtensionWithDot(multipartFile.getOriginalFilename());
        minioUtil.upload(multipartFile, prefix+avatarName);
        user.setAvatar(avatarName);
        usersService.updateById(user);
        return ApiResult.SUCCESS(user);
    }
    @ApiOperation("下载头像")
    @GetMapping("/avatar/{objectName}")
    public void downloadAvatar(@PathVariable("objectName") String objectName) throws MinioException, IOException {
        String prefix="avatar/";
        try{
            minioUtil.download(prefix+objectName, response);
        }catch (Exception e){
            // 头像不存在
        }
    }
}