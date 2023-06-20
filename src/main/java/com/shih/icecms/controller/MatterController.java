package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.User;
import com.shih.icecms.enums.ActionEnum;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.CommonUtil;
import com.shih.icecms.utils.MinioUtil;
import com.shih.icecms.utils.ShiroUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;

@RestController
@Slf4j
public class MatterController {
    @Autowired
    private MinioUtil minioUtil;
    @Resource
    private HttpServletRequest httpServletRequest;
    @Autowired
    private MatterService matterService;
    @Autowired
    private FileHistoryService fileHistoryService;
    @Autowired
    private ShiroUtil shiroUtil;
    @Autowired
    private MatterPermissionsService matterPermissionsService;
    @Resource
    private UsersService usersService;
    @ApiOperation(value = "创建文件夹")
    @PutMapping("/matter/addFolder")
    public ApiResult addFolder(@RequestParam(required = false) String parentId,@RequestParam String name){
        User user =shiroUtil.getLoginUser();
        if(parentId==null||parentId.equals(user.getId())){
            parentId=user.getId();
        }else{
            if(matterService.getById(parentId)==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        matterPermissionsService.checkMatterPermission(parentId, ActionEnum.Edit);
        if(matterService.count(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId,parentId).eq(Matter::getType,0).eq(Matter::getName,name))>0){
            return ApiResult.ERROR("已存在同名文件夹");
        }
        Matter matter=new Matter();
        matter.setCreateTime(new Date().getTime());
        matter.setName(name);
        matter.setCreator(user.getId());
        matter.setStatus(1);
        // 文件夹
        matter.setType(0);
        matter.setModifiedTime(new Date().getTime());
        matter.setParentId(parentId);
        matterService.save(matter);
        return ApiResult.SUCCESS(matter);
    }
    @ApiOperation(value = "生成临时访问地址")
    @PostMapping("/shared")
    public ApiResult GetTemporaryAccessUrl(String fileName){
        String sharedUrl = minioUtil.GetTemporaryAccessUrl(fileName,60*60);
        return ApiResult.SUCCESS(sharedUrl);
    }
    @GetMapping("/matter/list")
    @ApiOperation(value = "获取文件列表")
    public ApiResult list(
            @ApiParam(value = "文件夹id",defaultValue = "root",example = "root") @RequestParam(required = false) String matterId,
            @ApiParam(value = "页数",defaultValue = "1",example = "1") @RequestParam(required = false,defaultValue = "1") int pageNum,
            @ApiParam(value = "单页大小",defaultValue = "100",example = "100") @RequestParam(required = false,defaultValue = "100") int pageSize){
        User user =shiroUtil.getLoginUser();
        if(matterId==null){
            matterId=user.getId();
        }
        if(!matterId.equals(user.getId())){
            if(matterService.getById(matterId)==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.View);
        MatterDTO parentMatter=matterService.getMatterDtoById(matterId, user.getId());
        Page<MatterDTO> page = matterService.listByPage(matterId, user.getId(), pageNum,pageSize);
        parentMatter.setSubMatters(page);
        return ApiResult.SUCCESS(parentMatter);
    }

//    @Transactional
    @PostMapping("/matter/add")
    @ApiOperation(value = "上传文件")
    public ApiResult upload(@RequestParam(value = "file") MultipartFile multipartFile, @RequestParam(required = false) String matterId) {
        User user =shiroUtil.getLoginUser();
        if(!StringUtils.hasText(matterId)){
            matterId = user.getId();
        }
        // 能否对改文件夹内容进行修改
        matterPermissionsService.checkMatterPermission(StringUtils.hasText(matterId)?matterId: user.getId(), ActionEnum.Edit);
        FileHistory newHistory=new FileHistory();
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, matterId).eq(Matter::getType,1).eq(Matter::getName,multipartFile.getOriginalFilename()));
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
            matter.setParentId(matterId);
            matter.setCreateTime(new Date().getTime());
            newHistory.setVersion(1);
            saveMatter(user, newHistory, matter);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }
        return ApiResult.SUCCESS(newHistory);
    }

    private void saveMatter(User user, FileHistory newHistory, Matter matter) {
        matter.setModifiedTime(new Date().getTime());
        matterService.saveOrUpdate(matter);
        newHistory.setUserId(user.getId());
        newHistory.setCreated(new Date());
        newHistory.setDocKey(UUID.randomUUID().toString());
        newHistory.setMatterId(matter.getId());
        newHistory.setObjectName(matter.getParentId()+"/"+matter.getId() +"-"+newHistory.getVersion()+
                CommonUtil.getFilenameExtensionWithDot(matter.getName()));
        fileHistoryService.save(newHistory);
    }


    @ApiOperation(value = "文件下载")
    @GetMapping("/matter/download")
    public ApiResult download(@RequestParam String matterId, @RequestParam(required = false) String version, HttpServletResponse res) throws MinioException, IOException {
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.Download);
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId).eq(Matter::getType,1));
        FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                eq(version!=null,FileHistory::getVersion,version).
                eq(FileHistory::getMatterId,matter.getId()).
                orderBy(version==null,false,FileHistory::getCreated).
                last("limit 1"));
        if(fileHistory==null){
            return ApiResult.ERROR("version not exists");
        }
        res.setHeader("Version",fileHistory.getVersion().toString());
        minioUtil.download(fileHistory.getObjectName(),res, matter.getName());
        return ApiResult.SUCCESS();
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping("/matter/delete")
    public ApiResult delete(@RequestParam String matterId)   {
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId).eq(Matter::getType,1));
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
            matterService.removeById(matter.getId());
        }
        return ApiResult.SUCCESS("删除成功");
    }
    @ApiOperation(value = "文件下载")
    @GetMapping("/downloadByObjectName")
    public void downloadByObjectName(@RequestParam String objectName, HttpServletResponse res) throws MinioException, IOException {
        minioUtil.download(objectName,res,"changes.zip");
    }
    @ApiOperation(value = "重命名文件")
    @PutMapping("/matter/rename")
    public ApiResult rename(@RequestParam String matterId,@RequestParam String name){
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.Edit);
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId));
        if(matter!=null){
            matter.setName(name);
            matterService.saveOrUpdate(matter);
        }else{
            return ApiResult.ERROR("文件不存在");
        }
        return ApiResult.SUCCESS();
    }
    @ApiOperation(value = "请求历史版本列表")
    @GetMapping("/matter/version")
    public ApiResult<List<FileHistory>> matterVersion(@RequestParam String matterId){
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.View);
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId));
        List<FileHistory> fileHistories = fileHistoryService.list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId, matterId));
        return ApiResult.SUCCESS(fileHistories);
    }
}
