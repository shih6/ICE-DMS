package com.shih.icecms.controller;

import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.Users;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@RestController
public class MatterController {
    @Resource
    MatterService matterService;
    @Resource
    FileHistoryService fileHistoryService;
    @ApiOperation(value = "创建文件夹")
    @PostMapping("/matter/addFolder")
    public ApiResult addFolder(@RequestBody MatterDTO matterDTO){
        Users users=(Users) SecurityUtils.getSubject().getPrincipal();
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
    @ApiOperation(value = "添加文件")
    @PostMapping("/matter/add")
    public ApiResult addFile(@RequestBody MatterDTO matterDTO){
        Users users=(Users) SecurityUtils.getSubject().getPrincipal();
        Matter matter=new Matter();
        matter.setCreateTime(new Date().getTime());
        matter.setName(matter.getName());
        matter.setCreator(users.getId());
        matter.setStatus(1);
        // 文件
        matter.setType(1);
        matter.setModifiedTime(new Date().getTime());
        matter.setParentId(users.getId());
        matterService.save(matter);
        // 创建文件历史记录
        FileHistory fileHistory=new FileHistory();
        fileHistory.setMatterId(matter.getId());
        fileHistory.setCreated(new Date());
        fileHistory.setVersion(1);
        fileHistory.setUserId(users.getId());
        fileHistory.setDocKey(UUID.randomUUID().toString());
//        fileHistory.setServerVersion("");
//        fileHistory.setChangesObjectName("");
        fileHistoryService.save(fileHistory);
        return ApiResult.SUCCESS(matter);
    }
}
