package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.Users;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.CommonUtil;
import com.shih.icecms.utils.MinioUtil;
import com.shih.icecms.utils.ShiroUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
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
    @ApiOperation(value = "创建文件夹")
    @PostMapping("/matter/addFolder")
    public ApiResult addFolder(@RequestBody MatterDTO matterDTO){
        Users users=shiroUtil.getLoginUser();
        Matter matter=new Matter();
        matter.setCreateTime(new Date().getTime());
        matter.setName(matter.getName());
        matter.setCreator(users.getId());
        matter.setStatus(1);
        // 文件夹
        matter.setType(0);
        matter.setModifiedTime(new Date().getTime());
        matter.setParentId(users.getId());
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
            @ApiParam(value = "文件夹id",defaultValue = "root",example = "root") @RequestParam String matterId,
            @ApiParam(value = "页数",defaultValue = "1",example = "1") @RequestParam(required = false,defaultValue = "1") int pageNum,
            @ApiParam(value = "单页大小",defaultValue = "100",example = "100") @RequestParam(required = false,defaultValue = "100") int pageSize){
        Users users=shiroUtil.getLoginUser();
        Page<Matter> page = matterService.page(Page.of(pageNum, pageSize), new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, matterId));
        return ApiResult.SUCCESS(page);
    }

//    @Transactional
    @PostMapping("/matter/add")
    @ApiOperation(value = "上传文件")
    public ApiResult upload(@RequestParam(value = "file") MultipartFile multipartFile, @RequestParam(required = false) String matterId) {
        Users users=shiroUtil.getLoginUser();
        if(!StringUtils.hasText(matterId)){
            matterId =users.getId();
        }
        FileHistory newHistory=new FileHistory();
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId, matterId).eq(Matter::getType,1).eq(Matter::getName,multipartFile.getOriginalFilename()));
        if(matter !=null){
            List<FileHistory> fileHistoryList=fileHistoryService.getFileHistoryByMatterId(matter.getId());
            newHistory.setVersion(fileHistoryList.get(0).getVersion()+1);
            newHistory.setUserId(users.getId());
            newHistory.setCreated(new Date());
            newHistory.setDocKey(UUID.randomUUID().toString());
            newHistory.setMatterId(matter.getId());
            newHistory.setObjectName(matter.getParentId()+"/"+CommonUtil.getFileNameWithOutExt(matter.getName()) +"-"+newHistory.getVersion()+
                    CommonUtil.getFilenameExtensionWithDot(matter.getName()));
            matter.setModifiedTime(new Date().getTime());
            matterService.saveOrUpdate(matter);
            fileHistoryService.save(newHistory);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }else{
            matter=new Matter();
            matter.setCreator(users.getId());
            matter.setType(1);
            matter.setName(multipartFile.getOriginalFilename());
            matter.setParentId(matterId);
            matter.setModifiedTime(new Date().getTime());
            matter.setCreateTime(new Date().getTime());
            matterService.save(matter);
            newHistory.setVersion(1);
            newHistory.setUserId(users.getId());
            newHistory.setCreated(new Date());
            newHistory.setDocKey(UUID.randomUUID().toString());
            newHistory.setMatterId(matter.getId());
            newHistory.setObjectName(matter.getParentId()+"/"+CommonUtil.getFileNameWithOutExt(matter.getName()) +"-"+newHistory.getVersion()+
                    CommonUtil.getFilenameExtensionWithDot(matter.getName()));
            fileHistoryService.save(newHistory);
            minioUtil.upload(multipartFile, newHistory.getObjectName());
        }
        return ApiResult.SUCCESS(newHistory);
    }


    @ApiOperation(value = "文件下载")
    @GetMapping("/matter/download")
    public void download(@RequestParam String matterId, @RequestParam(required = false) String version, HttpServletResponse res) throws MinioException, IOException {
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId).eq(Matter::getType,1));
        FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                eq(version!=null,FileHistory::getVersion,version).
                eq(FileHistory::getMatterId,matter.getId()).
                orderBy(version==null,false,FileHistory::getCreated).
                last("limit 1"));
        minioUtil.download(fileHistory.getObjectName(),res, matter.getName());
    }
    @ApiOperation(value = "文件下载")
    @GetMapping("/downloadByObjectName")
    public void downloadByObjectName(@RequestParam String objectName, HttpServletResponse res) throws MinioException, IOException {
        minioUtil.download(objectName,res,"changes.zip");
    }
}
