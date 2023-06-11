package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.ChangesHistory;
import com.shih.icecms.dto.DocumentConfig;
import com.shih.icecms.dto.Track;
import com.shih.icecms.entity.FileChanges;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.service.FileChangesService;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.CommonUtil;
import com.shih.icecms.utils.MinioUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Api(tags = "onlyoffice相关接口")
@Slf4j
@RestController
@RequestMapping("/onlyoffice")
public class OnlyOfficeController {
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinioConfig prop;
    @Resource
    private FileHistoryService fileHistoryService;
    @Resource
    private FileChangesService fileChangesService;
    @Resource
    MatterService matterService;

    @GetMapping("/getDocumentConfig")
    @ApiOperation(value = "获取config")
    public ResponseEntity GetDocumentConfig(@RequestParam String matterId,@RequestParam(required = false) String currentVersion){
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId,matterId).eq(Matter::getType,1));
        if(matter !=null){
            FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId,matterId).orderBy(true,false,FileHistory::getCreated).last("limit 1"));
            DocumentConfig documentConfig=new DocumentConfig();
            documentConfig.setTitle(matter.getName());
            documentConfig.setUrl("http://192.168.0.112:8080/onlyoffice/downloadForOnlyOffice?matterId="+ matter.getId()+"&version="+ fileHistory.getVersion());
            documentConfig.setHistories(fileHistoryService.GetOnlyOfficeHistoryByFileId(matterId));
            documentConfig.setKey(fileHistory.getDocKey());
            return ResponseEntity.ok().body(documentConfig);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/callback")
    @ApiOperation(value = "onlyoffice回调")
    public ResponseEntity OnlyOfficeCallBack(@RequestBody Track track) throws MalformedURLException {
        log.info(track.getStatus().toString());
        log.info(track.toString());
        if(track.getStatus()==2||track.getStatus()==3){
            if(StringUtils.hasText(track.getKey())){
                FileHistory current = fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getDocKey,track.getKey()));
                Matter matter=matterService.getById(current.getMatterId());
                // 获取最新文档历史记录
                FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                        eq(FileHistory::getMatterId,matter.getId()).
                        orderBy(true,false,FileHistory::getCreated).
                        last("limit 1"));
                Integer version= fileHistory.getVersion();

                // 保存文档内容
                FileHistory newHistory=new FileHistory();
                newHistory.setCreated(new Date());
                if(track.getUsers()!=null&&track.getUsers().size()>0){
                    newHistory.setUserId(track.getUsers().get(0));
                }
                newHistory.setMatterId(matter.getId());
                newHistory.setDocKey(UUID.randomUUID().toString());
                newHistory.setVersion(version+1);
                newHistory.setObjectName(matter.getParentId()+"/"+ CommonUtil.getFileNameWithOutExt(matter.getName()) +"-"+newHistory.getVersion()+
                        CommonUtil.getFilenameExtensionWithDot(matter.getName()));
                newHistory.setServerVersion(track.getHistory().getServerVersion());
                // 保存changes.zip
                String objectName=newHistory.getObjectName()+"changes.zip";
                minioUtil.upload(new URL(track.getChangesurl()), objectName);
                newHistory.setChangesObjectName(objectName);

                minioUtil.upload(new URL(track.getUrl()), newHistory.getObjectName());
                fileHistoryService.save(newHistory);
                for (ChangesHistory change:track.getHistory().getChanges()) {
                    FileChanges fileChanges=new FileChanges();
                    fileChanges.setFileHistoryId(newHistory.getId());
                    fileChanges.setCreated(new Date());
                    fileChanges.setUserId(change.getUser().getId());
                    fileChangesService.save(fileChanges);
                }

            }

        }

        return ResponseEntity.ok().body("{\"error\":0}");
    }

    @ApiOperation(value = "文件下载")
    @GetMapping("/downloadForOnlyOffice")
    public ApiResult downloadForOnlyOffice(@RequestParam String matterId, @RequestParam(required = false) String version, HttpServletResponse res) throws MinioException, IOException {
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
    @ApiOperation(value = "changes文件下载")
    @GetMapping("/downloadChanges")
    public ApiResult downloadChanges(@RequestParam String matterId, @RequestParam String version, HttpServletResponse res) throws MinioException, IOException {
        FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                eq(version!=null,FileHistory::getVersion,version).
                eq(FileHistory::getMatterId,matterId).
                last("limit 1"));
        if(fileHistory==null){
            return ApiResult.ERROR("version not exists");
        }
        res.setHeader("Version",fileHistory.getVersion().toString());
        minioUtil.download(fileHistory.getChangesObjectName(),res, fileHistory.getChangesObjectName());
        return ApiResult.SUCCESS();
    }
}
