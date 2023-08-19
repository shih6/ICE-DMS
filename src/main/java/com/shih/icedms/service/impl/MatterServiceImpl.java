package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.dto.CreateDto;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.Matter;
import com.shih.icedms.entity.MatterPermissions;
import com.shih.icedms.entity.User;
import com.shih.icedms.enums.ActionEnum;
import com.shih.icedms.mapper.MatterMapper;
import com.shih.icedms.service.FileHistoryService;
import com.shih.icedms.service.MatterPermissionsService;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.utils.CommonUtil;
import com.shih.icedms.utils.MinioUtil;
import io.minio.errors.*;
import lombok.val;
import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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

    @Override
    public List<MatterDTO> list(String parentId, String userId,Integer type){
        List<MatterDTO> list = listAll(parentId, userId, type).stream().filter(p->p.getAction()>0).collect(Collectors.toList());
        list.forEach(i->{
            if(i.getSubMatters()==null){
                i.setSubMatters(new ArrayList<>());
            }
        });
        return list;
    }
    public List<MatterDTO> listAll(String parentId, String userId,Integer type){
        List<MatterDTO> list = baseMapper.list(parentId, userId, type, 31);
        list.forEach(i->{
            if(i.getSubMatters()==null){
                i.setSubMatters(new ArrayList<>());
            }
        });
        return list;
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
        List<MatterDTO> list = baseMapper.list(null, userId, null, 31);
        MatterDTO dto=null;
        for (MatterDTO p : list) {
            if (p.getId().equals(matterId)) {
                dto = p;
                if (dto.getSubMatters() == null) {
                    dto.setSubMatters(new ArrayList<>());
                }
                break;
            }

        }
        return dto;
    }
    @Override
    @Transactional(rollbackFor=Exception.class)
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
        matterDTO.setSubMatters(list(matter.getId(), user.getId(),null));
        return matterDTO;
    }

    /**
     * 创建Matter文件夹或模板实例
     * @param createDto DTO
     * @return MatterDTO
     * @throws IOException
     * @throws MinioException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Override
    @Transactional(rollbackFor=Exception.class)
    public MatterDTO create(CreateDto createDto) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        String parentMatterId = createDto.getParentMatterId();
        String fileName = createDto.getFileName();
        String fileType = createDto.getFileType();
        Boolean extendSuper = createDto.getExtendSuper();

        User user =(User) SecurityUtils.getSubject().getPrincipal();
        if(!StringUtils.hasText(parentMatterId)){
            parentMatterId = user.getId();
        }
        String templateName="";
        switch (fileType){
            case "":return getMatterDtoById(createDir(parentMatterId,fileName,user.getId(),extendSuper).getId(), user.getId());
            case "docx":templateName="base.docx";break;
            case "xlsx":templateName="base.xlsx";break;
            case "pptx":templateName="base.pptx";break;
            default:throw new FileNotFoundException(templateName+" 模板未定义");
        }
        File file= ResourceUtils.getFile("classpath:doc_template/"+templateName);
        if(!file.exists()){
            throw new FileNotFoundException(templateName+" 模板不存在");
        }
        // 能否对改文件夹内容进行修改
        matterPermissionsService.checkMatterPermission(StringUtils.hasText(parentMatterId)? parentMatterId : user.getId(), ActionEnum.Edit);
        FileHistory newHistory=new FileHistory();

        Matter matter=new Matter();
        matter.setCreator(user.getId());
        matter.setType(1);
        matter.setName(fileName);
        matter.setParentId(parentMatterId);
        matter.setCreateTime(new Date().getTime());
        matter.setExtendSuper(extendSuper);
        matter.setSize(file.length());
        saveOrUpdateMatter(user.getId(), newHistory, matter,1);
        minioUtil.upload(file, newHistory.getObjectName());
        MatterDTO matterDTO = getMatterDtoById(matter.getId(), user.getId());
        matterDTO.setSubMatters(list(matter.getId(), user.getId(),null));
        return matterDTO;
    }
    @Transactional
    public Matter createDir(String parentId,String name,String userId,Boolean extendSuper){
        matterPermissionsService.checkMatterPermission(parentId, ActionEnum.Edit);
        if(count(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId,parentId).eq(Matter::getType,0).eq(Matter::getName,name))>0){
            throw new RuntimeException("已存在同名文件夹");
        }
        Matter matter=new Matter();
        matter.setCreateTime(new Date().getTime());
        matter.setName(name);
        matter.setCreator(userId);
        matter.setStatus(1);
        matter.setExtendSuper(extendSuper);
        // 文件夹
        matter.setType(0);
        matter.setModifiedTime(new Date().getTime());
        matter.setParentId(parentId);
        save(matter);
        if(parentId.equals("public")){
            MatterPermissions matterPermissions=new MatterPermissions();
            matterPermissions.setAction(ActionEnum.View.getDesc());
            matterPermissions.setRoleId("0");
            matterPermissions.setRoleType(0);
            matterPermissions.setMatterId(matter.getId());
            matterPermissionsService.save(matterPermissions);
        }
        return matter;
    }
    @Transactional(rollbackFor=Exception.class)
    public void saveOrUpdateMatter(String userId, FileHistory newHistory, Matter matter,Integer version) {
        matter.setModifiedTime(new Date().getTime());
        // 补丁式修复
        boolean saveFlag=false;
        if(matter.getId()==null){
            saveFlag=true;
        }
        try{
            saveOrUpdate(matter);
        }catch (Exception e){
            log.error("插入失败:"+matter.toString());
            throw e;
        }
        // 执行save逻辑
        if(saveFlag){
            // 如果在根目录 自动添加权限
            if(matter.getParentId().equals("public")){
                MatterPermissions matterPermissions=new MatterPermissions();
                matterPermissions.setAction(ActionEnum.View.getDesc());
                matterPermissions.setRoleId("0");
                matterPermissions.setRoleType(0);
                matterPermissions.setMatterId(matter.getId());
                matterPermissionsService.save(matterPermissions);
            }
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
    @Transactional(rollbackFor=Exception.class)
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
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
    public Boolean deleteMatter(String matterId) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        Matter matter = getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId));
        long count = count(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, matterId));
        if(matter!=null&&count==0){
            matterPermissionsService.checkMatterPermission(matterId, ActionEnum.Delete);
            var fileHistorys=fileHistoryService.list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId,matter.getId()));
            for (FileHistory item:fileHistorys) {
                try {
                    minioUtil.delete(item.getObjectName());
                }catch (ConnectException e) {
                    throw e;
                }catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    log.warn("MatterID:"+matterId+"尝试删除不存在的文件"+e.getMessage());
                    throw e;
                }
                fileHistoryService.removeById(item.getId());
            }
            removeById(matter.getId());
            return true;
        }
        return false;
    }
    @Override
    @Transactional(rollbackFor=Exception.class)
    public List<String> deleteMatters(String matterIds) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        List<String> successList=new ArrayList<>();
        for (String matterId : matterIds.split(",")) {
            if(deleteMatter(matterId)){
                successList.add(matterId);
            }
        }
        return successList;
    }
    @Override
    public MatterDTO getTree(String matterId,String userId){
        MatterDTO root=getMatterDtoById(matterId, userId);
        List<MatterDTO> list = list(null, userId,0);
        // 构建树结构
        List<MatterDTO> cacheList=listAll(null,userId,0);
        for (int i=0;i<list.size();i++) {
            MatterDTO item = list.get(i);
            List<String> outPut = new ArrayList<>();
            getPath(item, cacheList, userId, outPut);
            for (int j = 1; j < outPut.size(); j++) {
                String parentId = outPut.get(j - 1);
                String currentId = outPut.get(j);
                // 在树中找到父节点
                MatterDTO parentMatterDTO = root.findNode(parentId);
                // 获取当前要放置的节点
                MatterDTO currentMatterDTO = cacheList.stream().filter(p -> p.getId().equals(currentId)).findFirst().get();
                // 如果当前的父节点无此节点则置入此节点
                if (parentMatterDTO.getSubMatters().stream().noneMatch(p -> p.getId().equals(currentMatterDTO.getId()))) {
                    parentMatterDTO.getSubMatters().add(currentMatterDTO);
                }
            }
        }
        return root;
    }

    public void getPath(MatterDTO matterDTO, List<MatterDTO> cacheList,String userId,@NotNull List<String> outPut){
        outPut.add(0,matterDTO.getId());
        if(matterDTO.getParentId().equals("")){
            return;
        }
        MatterDTO parentMatterDto = cacheList.stream().filter(p -> {
            return p.getId().equals(matterDTO.getParentId());
        }).findFirst().orElse(null);
        if(parentMatterDto==null){
            parentMatterDto=getMatterDtoById(matterDTO.getParentId(),userId);
            if(parentMatterDto!=null){
                cacheList.add(parentMatterDto);
                getPath(parentMatterDto,cacheList,userId, outPut);
            }
        }else{
            getPath(parentMatterDto,cacheList,userId, outPut);
        }
    }
}




