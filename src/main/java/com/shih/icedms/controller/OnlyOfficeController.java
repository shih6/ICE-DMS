package com.shih.icedms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icedms.config.MinioConfig;
import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.dto.ChangesHistory;
import com.shih.icedms.dto.DocumentConfig;
import com.shih.icedms.dto.Track;
import com.shih.icedms.entity.FileChanges;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.Matter;
import com.shih.icedms.service.FileChangesService;
import com.shih.icedms.service.FileHistoryService;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.utils.MinioUtil;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Api(tags = "onlyoffice相关接口")
@Slf4j
@RestController
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
    @Value("${setting.minioServerHost}")
    private String minioServerHost;
    @Value("${setting.documentServer.fileStorageServerHost}")
    private String fileStorageServerHost;


    @GetMapping("/onlyoffice/getDocumentConfig")
    @ApiOperation(value = "获取config")
    public ResponseEntity GetDocumentConfig(@RequestParam String matterId,@RequestParam(required = false) String currentVersion){
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId,matterId).eq(Matter::getType,1));
        if(matter !=null){
            FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId,matterId).orderBy(true,false,FileHistory::getCreated).last("limit 1"));
            DocumentConfig documentConfig=new DocumentConfig();
            documentConfig.setTitle(matter.getName());
            documentConfig.setUrl(fileStorageServerHost +"/onlyoffice/downloadForOnlyOffice?matterId="+ matter.getId()+"&version="+ fileHistory.getVersion());
            documentConfig.setHistories(fileHistoryService.GetOnlyOfficeHistoryByFileId(matterId));
            documentConfig.setKey(fileHistory.getDocKey());
            return ResponseEntity.ok().body(documentConfig);
        }
        return ResponseEntity.ok().body("{\"error\":3}");
    }

    @PostMapping("/onlyoffice/callback")
    @ApiOperation(value = "onlyoffice回调")
    public ResponseEntity OnlyOfficeCallBack(@RequestBody Track track) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        log.info(track.getStatus().toString());
        log.info(track.toString());
        try{
            if(track.getStatus()==2||track.getStatus()==3){
                if(StringUtils.hasText(track.getKey())){
                    FileHistory current = fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getDocKey,track.getKey()));
                    Matter matter=matterService.getById(current.getMatterId());
                    // 获取最新文档历史记录
                    FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                            eq(FileHistory::getMatterId,matter.getId()).
                            orderBy(true,false,FileHistory::getCreated).
                            last("limit 1"));
                    Integer currentVersion= fileHistory.getVersion();
                    String userId=null;
                    if(track.getUsers()!=null&&track.getUsers().size()>0) {
                        userId = track.getUsers().get(0);
                    }

                    // 保存 FileHistory 文档内容
                    FileHistory newHistory=new FileHistory();
                    matterService.saveOrUpdateMatter(userId,newHistory,matter,currentVersion+1);
                    StatObjectResponse statObjectResponse = minioUtil.upload(new URL(track.getUrl()), newHistory.getObjectName());
                    matter.setSize(statObjectResponse.size());

                    // 保存changes.zip
                    minioUtil.upload(new URL(track.getChangesurl()), newHistory.getObjectName()+"changes.zip");
                    newHistory.setServerVersion(track.getHistory().getServerVersion());
                    newHistory.setChangesObjectName(newHistory.getObjectName()+"changes.zip");
                    fileHistoryService.updateById(fileHistory);


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
        }catch (Exception e){
            log.error("only office回调");
            log.error(e.getMessage());
            return ResponseEntity.ok().body("{\"error\":3}");
        }
    }

    @ApiOperation(value = "文件下载")
    @RequestMapping("/onlyoffice/downloadForOnlyOffice")
    public ResponseEntity downloadForOnlyOffice(@RequestParam String matterId, @RequestParam(required = false) String version, HttpServletResponse res) throws MinioException, IOException {
        try {
            Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId).eq(Matter::getType,1));
            FileHistory fileHistory=fileHistoryService.getOne(new LambdaQueryWrapper<FileHistory>().
                    eq(version!=null,FileHistory::getVersion,version).
                    eq(FileHistory::getMatterId,matter.getId()).
                    orderBy(version==null,false,FileHistory::getCreated).
                    last("limit 1"));
            if(fileHistory==null){
                return ResponseEntity.ok(ApiResult.ERROR("version not exists"));
            }
            res.setHeader("Version",fileHistory.getVersion().toString());
            minioUtil.download(fileHistory.getObjectName(),res, matter.getName());
        }catch (Exception e){
            return ResponseEntity.ok().body("{\"error\":3}");
        }

        return ResponseEntity.ok(ApiResult.SUCCESS());
    }
    @ApiOperation(value = "changes文件下载")
    @GetMapping("/onlyoffice/downloadChanges")
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
