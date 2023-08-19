package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.dto.*;
import com.shih.icedms.entity.*;
import com.shih.icedms.enums.ActionEnum;
import com.shih.icedms.mapper.MatterPermissionsMapper;
import com.shih.icedms.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
* @author 1
* @description 针对表【matter_permissions】的数据库操作Service实现
* @createDate 2023-06-04 20:47:35
*/
@Service
public class MatterPermissionsServiceImpl extends ServiceImpl<MatterPermissionsMapper, MatterPermissions>
    implements MatterPermissionsService{
    private MatterService matterService;
    private UserRolesService userRolesService;
    private RoleService roleService;
    private UsersService usersService;
    @Autowired
    @Lazy
    public void setMatterService(MatterService matterService) {
        this.matterService = matterService;
    }
    @Autowired
    public void setUserRolesService(UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
    }
    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }
    @Autowired
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public int getMatterPermission(String matterId, String userId) {
/*        // 根目录
        if(Objects.equals(matterId, userId)){
            return ActionEnum.AccessControl.getDesc();
        }
        Matter matter=matterService.getById(matterId);
        if(matter.getCreator().equals(userId)){
            return ActionEnum.AccessControl.getDesc();
        }
        List<MatterPermissions> matterPermissions=list(new LambdaQueryWrapper<MatterPermissions>().eq(MatterPermissions::getMatterId,matterId));
        if(matterPermissions.size()==0){
            return 0;
        }

        int actionNum=0;
        for (MatterPermissions item:matterPermissions) {
            // Role
            if(item.getRoleType()==0){
                List<UserRoles> userRolesList=userRolesService.list(new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getRoleId,item.getRoleId()));
                if(userRolesList.stream().filter(i->i.getUserId().equals(userId)).findAny().orElse(null)!=null){
                    actionNum=actionNum|item.getAction();
                }
            }
            // User
            if(item.getRoleType()==1){
                actionNum=actionNum|item.getAction();
            }
        };*/
        MatterDTO matterDtoById = matterService.getMatterDtoById(matterId, userId);
        Integer action = matterDtoById.getAction();
        return action;
    }
    public boolean checkMatterPermission(String matterId, ActionEnum actionEnum){
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        if(actionEnum.getDesc()==ActionEnum.Delete.getDesc()){
            switch (matterId){
                case "root":
                case "public":
                case "private":
                    return false;
            }
            if(user.getId().equals(matterId)){
                return false;
            }
        }
        int actionNum=getMatterPermission(matterId,user.getId());
        if(actionNum==ActionEnum.AccessControl.getDesc()){
            return true;
        }
        if((actionEnum.getDesc() & actionNum) != 0){
            return true;
        }
        log.error(String.format("权限不足，userId：[%s] 尝试获取Matter：[%s] [%s]权限",user.getId(),matterId,actionEnum.toString()));
        throw new AuthenticationException("权限不足");
    }

    @Override
    public boolean checkRoleExists(String roleId, int roleType) {
        if(roleType==0){
            return roleService.hasRole(roleId);
        }
        if(roleType==1){
            return usersService.getById(roleId)!=null;
        }
        return false;
    }
    public List<AccessRoleDto> accessRoleListByMatterId(String matterId){
        return baseMapper.accessRoleListByMatterId(matterId);
    }
    @Transactional(rollbackFor = {Exception.class})
    public List<AccessRoleDto> addPermission(List<MatterActionDto> matterActionDtoList){
        List<AccessRoleDto> accessRoleDtoList=new ArrayList<>();
        User user=(User)SecurityUtils.getSubject().getPrincipal();
        for (MatterActionDto matterActionDto:matterActionDtoList) {
            // 检查是否存在文件
            if(!matterActionDto.getMatterId().equals(user.getId())){
                if(matterService.getById(matterActionDto.getMatterId())==null){
                    throw new RuntimeException("文件不存在");
                }
            }
            // 检查权限
            checkMatterPermission(matterActionDto.getMatterId(), ActionEnum.AccessControl);
            // 检查角色是否存在
            if(checkRoleExists(matterActionDto.getRoleId(),matterActionDto.getRoleType())){
                // 是否重复添加
                MatterPermissions permissions=getOne(new LambdaQueryWrapper<MatterPermissions>().
                        eq(MatterPermissions::getMatterId,matterActionDto.getMatterId()).
                        eq(MatterPermissions::getRoleId,matterActionDto.getRoleId()).
                        eq(MatterPermissions::getRoleType,matterActionDto.getRoleType()));
                if(permissions!=null){
                    permissions.setAction(matterActionDto.getActionNum());
                    updateById(permissions);
                }else{
                    permissions = new MatterPermissions();
                    permissions.setMatterId(matterActionDto.getMatterId());
                    permissions.setRoleId(matterActionDto.getRoleId());
                    permissions.setRoleType(matterActionDto.getRoleType());
                    permissions.setAction(matterActionDto.getActionNum());
                    save(permissions);
                }
                AccessRoleDto accessRoleDto=new AccessRoleDto(permissions.getId(), permissions.getMatterId(),
                        permissions.getRoleId(), permissions.getRoleType(), permissions.getAction(), null,null,null,null );
                if(permissions.getRoleType()==0){
                    Role role=roleService.getById(permissions.getRoleId());
                    accessRoleDto.setRoleName(role.getRoleName());
                    accessRoleDto.setRoleDesc(role.getRoleDesc());
                }
                if(permissions.getRoleType()==1){
                    User role=usersService.getById(permissions.getRoleId());
                    accessRoleDto.setRoleName(role.getActualName());
                    accessRoleDto.setRoleDesc(role.getUsername());
                    accessRoleDto.setAvatar(role.getAvatar());
                }
                accessRoleDtoList.add(accessRoleDto);
            }else{
                throw new RuntimeException("角色不存在");
            }
        }
        return accessRoleDtoList;
    }
}




