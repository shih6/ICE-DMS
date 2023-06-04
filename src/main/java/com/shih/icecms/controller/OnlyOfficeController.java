package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.ChangesHistory;
import com.shih.icecms.dto.DocumentConfig;
import com.shih.icecms.dto.Track;
import com.shih.icecms.entity.FileChanges;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.service.FileChangesService;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    public ResponseEntity GetDocumentConfig(@RequestParam String fileKey){
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getFileId,fileKey).eq(Matter::getType,1).orderBy(true,false, Matter::getVersion).last("limit 1"));
        if(matter !=null){
            DocumentConfig documentConfig=new DocumentConfig();
            documentConfig.setTitle(matter.getName());
            documentConfig.setUrl("http://192.168.0.112:8080/download?fileId="+ matter.getFileId()+"&fileVersion="+ matter.getVersion()+"&fileType="+ matter.getType());
            documentConfig.setHistories(fileHistoryService.GetHistoryByFileId(fileKey));
            documentConfig.setKey(fileKey);
            return ResponseEntity.ok().body(documentConfig);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/callback")
    @ApiOperation(value = "onlyoffice回调")
    public ResponseEntity OnlyOfficeCallBack(@RequestBody Track track) throws MalformedURLException {
        log.info(track.toString());
        if(track.getStatus()==2||track.getStatus()==3){
            if(StringUtils.hasText(track.getKey())){
                // 获取当前文档版本
                Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getFileId,track.getKey()).eq(Matter::getType,1).last("limit 1"));
                Integer version= matter.getVersion();
                String fileId= matter.getFileId();
                if(version==1){
                    FileHistory fileHistory=new FileHistory(null,track.getKey(), UUID.randomUUID().toString(),new Date(),"test",1,track.getHistory().getServerVersion(),null);
                    fileHistoryService.save(fileHistory);
                }
                version++;
                // 保存文档内容
                Matter tmp=new Matter();
                tmp.setVersion(version);
                tmp.setPath(fileId+"/"+version+ matter.getName().substring(matter.getName().lastIndexOf(".")));
                tmp.setCreator("test");
                tmp.setType(1);
                tmp.setName(matter.getName());
                tmp.setFileId(matter.getFileId());
                tmp.setParentId(matter.getParentId());
                tmp.setStatus(1);
                minioUtil.upload(new URL(track.getUrl()), tmp.getPath());
                matterService.save(tmp);
                // 保存changes.zip
                String objectName=fileId+"/"+version+".zip";
                minioUtil.upload(new URL(track.getChangesurl()), objectName);
                if(matter !=null){
                    FileHistory fileHistory=new FileHistory(null,track.getKey(), UUID.randomUUID().toString(),new Date(),"test",version,track.getHistory().getServerVersion(),objectName);
                    fileHistoryService.save(fileHistory);
                    for (ChangesHistory change:track.getHistory().getChanges()) {
                        FileChanges fileChanges=new FileChanges();
                        fileChanges.setFileHistoryId(fileHistory.getId());
                        fileChanges.setCreated(new Date());
                        fileChanges.setUserId(change.getUser().getId());
                        fileChangesService.save(fileChanges);
                    }
                }
            }

        }

        return ResponseEntity.ok().body("{\"error\":0}");
    }
}
