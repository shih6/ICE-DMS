package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.dto.RoleDto;
import com.shih.icedms.entity.Role;
import com.shih.icedms.entity.User;
import com.shih.icedms.entity.UserRoles;
import com.shih.icedms.service.RoleService;
import com.shih.icedms.mapper.RoleMapper;
import com.shih.icedms.service.UserRolesService;
import com.shih.icedms.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 1
* @description 针对表【role】的数据库操作Service实现
* @createDate 2023-06-06 22:41:31
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{
    @Resource
    private UserRolesService userRolesService;
    @Resource
    private UsersService usersService;
    public boolean hasRole(String roleId){
        return getOne(new LambdaQueryWrapper<Role>().eq(Role::getId, roleId)) != null;
    }
    public List<RoleDto> roleList(){
        List<Role> roleList=list();
        List<RoleDto> roleDtos=roleList.stream().map(i->{
            RoleDto dto = new RoleDto();
            BeanUtils.copyProperties(i,dto);
            return dto;
        }).collect(Collectors.toList());
        roleDtos.forEach(i->{
            i.setUsers(usersService.listByIds(
                    userRolesService.list(
                            new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getRoleId,i.getId())).stream().map(j->j.getUserId()).collect(Collectors.toList())
                    )
            );
        });
        return roleDtos;
    }
    @Transactional(rollbackFor = {Exception.class})
    public Boolean createRole(RoleDto roleDto){
        Role role=new Role();
        role.setRoleDesc(roleDto.getRoleDesc());
        role.setRoleName(roleDto.getRoleName());
        role.setCreator(role.getCreator());
        role.setCreateTime(new Date());
        save(role);
        List<User> userList=usersService.listByIds(roleDto.getUsers().stream().map(User::getId).collect(Collectors.toList()));
        userRolesService.saveBatch(userList.stream().map(p->{
            UserRoles userRoles = new UserRoles();
            userRoles.setUserId(p.getId());
            userRoles.setRoleId(role.getId());
            return userRoles;
        }).collect(Collectors.toList()));
        return true;
    }
}




