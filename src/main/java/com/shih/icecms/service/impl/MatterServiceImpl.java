package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.User;
import com.shih.icecms.enums.ActionEnum;
import com.shih.icecms.mapper.MatterMapper;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.CommonUtil;
import com.shih.icecms.utils.MinioUtil;
import io.minio.errors.MinioException;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    public void setMinioUtil(MinioUtil minioUtil) {
        this.minioUtil = minioUtil;
    }

    public Page<MatterDTO> listByPage(String matterId, String userId, int pageNum, int pageSize){
        return baseMapper.listByPage(Page.of(pageNum,pageSize),matterId, userId, 31);
    }

    @Override
    public MatterDTO getMatterDtoById(String matterId, String userId) {
        return baseMapper.getMatterDtoById(matterId, userId, 31);
    }

    @Override
    @Transactional
    public MatterDTO uploadFile(MultipartFile multipartFile, String parentMatterId){
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
            newHistory.setVersion(fileHistoryList.get(0).getVersion()+1);
            saveMatter(user, newHistory, matter);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }else{
            matter=new Matter();
            matter.setCreator(user.getId());
            matter.setType(1);
            matter.setName(multipartFile.getOriginalFilename());
            matter.setParentId(parentMatterId);
            matter.setCreateTime(new Date().getTime());
            newHistory.setVersion(1);
            saveMatter(user, newHistory, matter);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }
        MatterDTO matterDTO = getMatterDtoById(matter.getId(), user.getId());
        Page<MatterDTO> page = listByPage(matter.getId(), user.getId(), 0,0);
        matterDTO.setSubMatters(page);
        return matterDTO;
    }
    @Transactional
    public void saveMatter(User user, FileHistory newHistory, Matter matter) {
        matter.setModifiedTime(new Date().getTime());
        saveOrUpdate(matter);
        newHistory.setUserId(user.getId());
        newHistory.setCreated(new Date());
        newHistory.setDocKey(UUID.randomUUID().toString());
        newHistory.setMatterId(matter.getId());
        newHistory.setObjectName(matter.getParentId()+"/"+matter.getId() +"-"+newHistory.getVersion()+
                CommonUtil.getFilenameExtensionWithDot(matter.getName()));
        fileHistoryService.save(newHistory);
    }
    @Transactional
    public Boolean deleteMatter(String matterId){
        Matter matter = getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId).eq(Matter::getType,1));
        if(matter!=null){
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
}




