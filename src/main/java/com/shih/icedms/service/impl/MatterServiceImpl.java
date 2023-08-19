package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.*;
import com.shih.icedms.enums.ActionEnum;
import com.shih.icedms.mapper.MatterMapper;
import com.shih.icedms.service.*;
import com.shih.icedms.utils.CommonUtil;
import com.shih.icedms.utils.MinioUtil;
import io.minio.errors.*;
import lombok.val;
import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service实现
* @createDate 2023-06-04 21:16:46
*/
@Service
public class MatterServiceImpl extends ServiceImpl<MatterMapper, Matter>
    implements MatterService{
    private MatterPermissionsService matterPermissionsService;
    private FileHistoryService fileHistoryService;
    private UserRolesService userRolesService;
    private UsersService usersService;
    private MinioUtil minioUtil;
    @Autowired
    public void setMatterPermissionsService(MatterPermissionsService matterPermissionsService) {
        this.matterPermissionsService = matterPermissionsService;
    }
    @Autowired
    public void setFileHistoryService(FileHistoryService fileHistoryService) {
        this.fileHistoryService = fileHistoryService;
    }
    @Autowired
    public void setUserRolesService(UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
    }
    @Autowired
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    @Autowired
    public void setMinioUtil(MinioUtil minioUtil) {
        this.minioUtil = minioUtil;
    }

    @Override
    public List<MatterDTO> listOfDto(String userId,Integer type){
        List<MatterDTO> matterDTOList = null;
        if(type==null){
            matterDTOList=baseMapper.listOfDtoWithoutAction();
        }else{
            if(type==0){
                matterDTOList=baseMapper.listOfDtoWithoutAction().stream().filter(p->p.getType()==0).collect(Collectors.toList());
            }
            if(type==1){
                matterDTOList=baseMapper.listOfDtoWithoutAction().stream().filter(p->p.getType()<=1).collect(Collectors.toList());
            }
        }
        List<MatterPermissions> permissions=matterPermissionsService.list();
        List<UserRoles> userRoles=userRolesService.list();
        User user=usersService.getById(userId);
        for (MatterDTO currentNode : matterDTOList) {
            currentNode.setSubMatters(new ArrayList<>());
            int action=0;
            //管理员
            if(user.getIsAdmin()!=null&&user.getIsAdmin()==1){
                action=ActionEnum.AccessControl.getDesc();
            }
            //文件夹创建者
            if(currentNode.getCreator()!=null&&currentNode.getCreator().equals(userId)){
                action= ActionEnum.AccessControl.getDesc();
            }
            // 权限设置列表
            List<MatterPermissions> matterPermissions = permissions.stream().filter(p -> p.getMatterId().equals(currentNode.getId())).collect(Collectors.toList());
            for (MatterPermissions permission : matterPermissions) {
                if(ActionEnum.AccessControl.getDesc()==action){
                    break;
                }
                // 用户组
                if (permission.getRoleType() == 0) {
                    if (userRoles.stream().anyMatch(p -> p.getRoleId().toString().equals(permission.getRoleId()) &&
                            p.getUserId().equals(userId))) {
                        action = action | permission.getAction();
                    }
                }
                //用户
                if(permission.getRoleType()==1){
                    if(userId.equals(permission.getRoleId())){
                        action=action|permission.getAction();
                    }
                }
            }
            currentNode.setAction(action);
        }
        for (MatterDTO currentNode : matterDTOList) {
            // 检查是否继承上级文件
            if(currentNode.getExtendSuper()!=null&& currentNode.getExtendSuper()){
                MatterDTO preNode=matterDTOList.stream().filter(p->p.getId().equals(currentNode.getParentId())).findAny().orElse(null);
                if(preNode!=null){
                    currentNode.setAction(currentNode.getAction()|preNode.getAction());
                }
            }

        }
        return matterDTOList;
    }
    @Override
    public Page<MatterDTO> listSearch(Page page,String matterName){
        User user =(User) SecurityUtils.getSubject().getPrincipal();
        Page<MatterDTO> list = baseMapper.listSearch(page,matterName, user.getId(), 31);
        list.getRecords().forEach(i->{
            if(i.getSubMatters()==null){
                i.setSubMatters(new ArrayList<>());
            }
        });
        return list;
    }
    @Override
    public MatterDTO getMatterDtoById(String matterId, String userId) {
        User user=usersService.getById(userId);
        List<MatterDTO> list = listOfDto( userId,null);
        MatterDTO dto=list.stream().filter(p->p.getId().equals(matterId)).findAny().orElse(null);
        dto.setSubMatters(listOfDto(user.getId(),null).stream().filter(p->p.getAction()>0&&
                p.getParentId().equals(matterId)).collect(Collectors.toList()));
        return dto;
    }
    @Override
    @Transactional
    public MatterDTO uploadFile(MultipartFile multipartFile, String parentMatterId) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        User user =(User) SecurityUtils.getSubject().getPrincipal();
        if(!StringUtils.hasText(parentMatterId)){
            parentMatterId = user.getId();
        }
        // 能否对改文件夹内容进行修改
        matterPermissionsService.checkMatterPermission(StringUtils.hasText(parentMatterId)? parentMatterId : user.getId(), ActionEnum.Edit);
        FileHistory newHistory=new FileHistory();
        Matter matter = getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, parentMatterId).eq(Matter::getType,1).eq(Matter::getName,multipartFile.getOriginalFilename()));
        if(matter !=null){
            // 能否覆盖此文件
            matterPermissionsService.checkMatterPermission(matter.getId(), ActionEnum.Edit);
            List<FileHistory> fileHistoryList=fileHistoryService.getFileHistoryByMatterId(matter.getId());
            matter.setSize(multipartFile.getSize());
            saveOrUpdateMatter(user.getId(), newHistory, matter,fileHistoryList.get(0).getVersion()+1);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }else{
            matter=new Matter();
            matter.setCreator(user.getId());
            matter.setType(1);
            matter.setName(multipartFile.getOriginalFilename());
            matter.setParentId(parentMatterId);
            matter.setCreateTime(new Date().getTime());
            matter.setSize(multipartFile.getSize());
            saveOrUpdateMatter(user.getId(), newHistory, matter,1);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }
        MatterDTO matterDTO = getMatterDtoById(matter.getId(), user.getId());

        return matterDTO;
    }
    @Transactional
    public void saveOrUpdateMatter(String userId, FileHistory newHistory, Matter matter,Integer version) {
        matter.setModifiedTime(new Date().getTime());
        try{
            saveOrUpdate(matter);
        }catch (Exception e){
            log.error("插入失败:"+matter.toString());
            throw e;
        }
        newHistory.setVersion(version);
        newHistory.setUserId(userId);
        newHistory.setCreated(new Date());
        newHistory.setDocKey(UUID.randomUUID().toString());
        newHistory.setMatterId(matter.getId());
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String yyyy_mm_dd = dateFormat.format(new Date());
        // [2023-04-12/matterId-version-xxxx] or [2023-04-12/matterId-version-xxxx.extension]
        String objName=yyyy_mm_dd+"/"+matter.getId()+"-"+newHistory.getVersion()+"-"+UUID.randomUUID().toString().substring(0,4) +
                CommonUtil.getFilenameExtensionWithDot(matter.getName());
        newHistory.setObjectName(objName);
        fileHistoryService.save(newHistory);
    }
    @Transactional
    public boolean move(String matterId,String target){
        User user =(User) SecurityUtils.getSubject().getPrincipal();
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.AccessControl);
        Matter matter = getById(matterId);
        Matter targetMatter=getById(target);
        if(targetMatter==null||matter==null){
            return false;
        }
        matter.setParentId(target);
        updateById(matter);
        return true;
    }
    @Override
    @Transactional
    public Boolean deleteMatter(String matterId){
        Matter matter = getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId));
        long count = count(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, matterId));
        if(matter!=null&&count==0){
            matterPermissionsService.checkMatterPermission(matterId, ActionEnum.Delete);
            var fileHistorys=fileHistoryService.list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId,matter.getId()));
            for (FileHistory item:fileHistorys) {
                try {
                    minioUtil.delete(item.getObjectName());
                } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    log.error(e.getMessage());
//                    throw new RuntimeException(e);
                }
                fileHistoryService.removeById(item.getId());
            }
            removeById(matter.getId());
            return true;
        }
        return false;
    }
    @Override
    @Transactional
    public List<String> deleteMatters(String matterIds){
        List<String> successList=new ArrayList<>();
        for (String matterId : matterIds.split(",")) {
            if(deleteMatter(matterId)){
                successList.add(matterId);
            }
        }
        return successList;
    }

    /***
     *
     * @param userId
     * @param type 0为仅文件夹 1为包含非文件夹
     * @return
     */
    public MatterDTO getTreeV2(String userId,int type){
        List<MatterDTO> matterDTOList = listOfDto(userId,type);
        //找到根节点
        MatterDTO root=matterDTOList.stream().filter(p->p.getId().equals("root")).findAny().orElse(null);
        if(root==null){
            throw new RuntimeException("根节点不存在");
        }
        // 移除root节点 防止重复添加
        matterDTOList.remove(root);

        List<MatterDTO> hasRights = matterDTOList.stream().filter(p -> p.getAction() > 0).collect(Collectors.toList());
        List<MatterDTO> tmpNode=new ArrayList<>();
        for (MatterDTO current : hasRights) {
            MatterDTO preNode=matterDTOList.stream().filter(p->p.getId().equals(current.getParentId())).findAny().orElse(null);
            if(preNode!=null){
                if(!tmpNode.contains(preNode)&&!hasRights.contains(preNode)){
                    tmpNode.add(preNode);
                }
            }
        }
        hasRights.addAll(tmpNode);


        for (MatterDTO currentNode : hasRights) {
            if(root.getId().equals(currentNode.getParentId())){
                if(root.getSubMatters()==null){
                    root.setSubMatters(new ArrayList<>());
                }
                root.getSubMatters().add(currentNode);
            }
            if(currentNode.getSubMatters()==null){
                currentNode.setSubMatters(new ArrayList<>());
            }
            // 二次循环寻找子节点
            for (MatterDTO nextNode : matterDTOList) {
                if(currentNode.getId().equals(nextNode.getParentId())){
                    if(hasRights.contains(nextNode)){
                        currentNode.getSubMatters().add(nextNode);
                    }
                }
            }
        }
        return root;
    }

}




