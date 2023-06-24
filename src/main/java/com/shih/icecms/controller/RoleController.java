package com.shih.icecms.controller;

import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.RoleDto;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.RoleService;
import com.shih.icecms.utils.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
public class RoleController {
    @Resource
    private ShiroUtil shiroUtil;
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
        User user=shiroUtil.getLoginUser();
        return ApiResult.SUCCESS(roleService.roleList());
    }
    @ApiOperation("删除Role")
    @DeleteMapping("/role")
    public ApiResult deleteRole(){
        return ApiResult.SUCCESS();
    }
}
