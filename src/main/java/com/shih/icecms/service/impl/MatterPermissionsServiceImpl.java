package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.MatterPermissions;
import com.shih.icecms.entity.UserRoles;
import com.shih.icecms.entity.User;
import com.shih.icecms.enums.ActionEnum;
import com.shih.icecms.mapper.MatterPermissionsMapper;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UserRolesService;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 1
* @description 针对表【matter_permissions】的数据库操作Service实现
* @createDate 2023-06-04 20:47:35
*/
@Service
public class MatterPermissionsServiceImpl extends ServiceImpl<MatterPermissionsMapper, MatterPermissions>
    implements MatterPermissionsService{
    @Resource
    MatterService matterService;
    @Resource
    UserRolesService userRolesService;

    public Integer getMatterPermission(String matterId,String userId) {
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
        };

        return actionNum;
    }
    public boolean checkMatterPermission(String matterId, ActionEnum actionEnum){
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        Integer actionNum=getMatterPermission(matterId,user.getId());
        if(actionNum==ActionEnum.AccessControl.getDesc()){
            return true;
        }
        return (actionEnum.getDesc() & actionNum) != 0;
    }

}




