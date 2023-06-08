package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterActionDto;
import com.shih.icecms.entity.MatterPermissions;
import com.shih.icecms.entity.User;
import com.shih.icecms.enums.ActionEnum;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.ShiroUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class MatterPermissionController {
    @Resource
    private HttpServletRequest httpServletRequest;
    @Autowired
    private MatterService matterService;
    @Autowired
    private ShiroUtil shiroUtil;
    @Autowired
    private MatterPermissionsService matterPermissionsService;
    @ApiOperation("添加权限")
    @PutMapping("/matter/access")
    public ApiResult addPermission(@RequestBody MatterActionDto matterActionDto){
        User user=shiroUtil.getLoginUser();
        // 检查权限
        matterPermissionsService.checkMatterPermission(matterActionDto.getMatterId(), ActionEnum.AccessControl);
        // 检查是否存在文件
        if(!matterActionDto.getMatterId().equals(user.getId())){
            if(matterService.getById(matterActionDto.getMatterId())==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        // 检查角色是否存在
        if(matterPermissionsService.checkRoleExists(matterActionDto.getRoleId(),matterActionDto.getRoleType())){
            // 是否重复添加
            if(matterPermissionsService.count(new LambdaQueryWrapper<MatterPermissions>().
                    eq(MatterPermissions::getMatterId,matterActionDto.getMatterId()).
                    eq(MatterPermissions::getRoleId,matterActionDto.getRoleId()).
                    eq(MatterPermissions::getRoleType,matterActionDto.getRoleType()))>0){
                return ApiResult.ERROR("重复添加");
            }
            MatterPermissions permissions = new MatterPermissions();
            permissions.setMatterId(matterActionDto.getMatterId());
            permissions.setRoleId(matterActionDto.getRoleId());
            permissions.setRoleType(matterActionDto.getRoleType());
            permissions.setAction(matterActionDto.getActionNum());
            matterPermissionsService.save(permissions);
            return ApiResult.SUCCESS(permissions);
        }else{
            return ApiResult.ERROR("角色不存在");
        }
    }
    @ApiOperation("修改权限")
    @PostMapping("/matter/access")
    public ApiResult editPermission(@RequestBody MatterActionDto matterActionDto){
        User user=shiroUtil.getLoginUser();
        // 检查权限
        matterPermissionsService.checkMatterPermission(matterActionDto.getMatterId(), ActionEnum.AccessControl);
        // 检查是否存在文件
        if(!matterActionDto.getMatterId().equals(user.getId())){
            if(matterService.getById(matterActionDto.getMatterId())==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        // 检查角色是否存在
        if(matterPermissionsService.checkRoleExists(matterActionDto.getRoleId(),matterActionDto.getRoleType())){
            MatterPermissions permissions=matterPermissionsService.getOne(new LambdaQueryWrapper<MatterPermissions>().
                    eq(MatterPermissions::getMatterId,matterActionDto.getMatterId()).
                    eq(MatterPermissions::getRoleId,matterActionDto.getRoleId()).
                    eq(MatterPermissions::getRoleType,matterActionDto.getRoleType()));
            if(permissions==null){
                return ApiResult.ERROR("目标不存在");
            }
            permissions.setAction(matterActionDto.getActionNum());
            matterPermissionsService.updateById(permissions);
            return ApiResult.SUCCESS(permissions);
        }else{
            return ApiResult.ERROR("角色不存在");
        }
    }
    @ApiOperation("删除权限")
    @DeleteMapping ("/matter/access")
    public ApiResult deletePermission(@RequestParam String matterId,@RequestParam String permissionId){
        User user=shiroUtil.getLoginUser();
        // 检查权限
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.AccessControl);
        // 检查是否存在文件
        if(!matterId.equals(user.getId())){
            if(matterService.getById(matterId)==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        // 检查角色是否存在
        matterPermissionsService.removeById(permissionId);
        return ApiResult.SUCCESS();
    }
}
