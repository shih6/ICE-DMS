package com.shih.icedms.controller;

import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.dto.RoleDto;
import com.shih.icedms.entity.User;
import com.shih.icedms.service.MatterPermissionsService;
import com.shih.icedms.service.RoleService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
public class RoleController {
    @Resource
    private RoleService roleService;
    @Resource
    private MatterPermissionsService matterPermissionsService;

    @ApiOperation("创建Role")
    @PutMapping("/role")
    public ApiResult createRole(@RequestBody RoleDto roleDto){
        return ApiResult.SUCCESS();
    }
    @ApiOperation("list")
    @GetMapping("/role")
    public ApiResult list(){
        User user=(User) SecurityUtils.getSubject().getPrincipal();
        return ApiResult.SUCCESS(roleService.roleList());
    }
    @ApiOperation("删除Role")
    @DeleteMapping("/role")
    public ApiResult deleteRole(){
        return ApiResult.SUCCESS();
    }
}
