package com.shih.icedms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.dto.CreateDto;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.dto.MatterSearchDTO;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.Matter;
import com.shih.icedms.entity.MatterPermissions;
import com.shih.icedms.entity.User;
import com.shih.icedms.enums.ActionEnum;
import com.shih.icedms.service.FileHistoryService;
import com.shih.icedms.service.MatterPermissionsService;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.service.UsersService;
import com.shih.icedms.utils.JwtUtil;
import com.shih.icedms.utils.MinioUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.minio.errors.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class MatterController {
    @Resource
    private MinioUtil minioUtil;
    private MatterService matterService;
    @Resource
    private FileHistoryService fileHistoryService;
    private MatterPermissionsService matterPermissionsService;
    @Resource
    private UsersService usersService;
    @Autowired
    public void setMatterService(MatterService matterService) {
        this.matterService = matterService;
    }
    @Autowired
    public void setMatterPermissionsService(MatterPermissionsService matterPermissionsService) {
        this.matterPermissionsService = matterPermissionsService;
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
            @ApiParam(value = "文件夹id",defaultValue = "root",example = "root") @RequestParam(required = false) String matterId){
        User user =(User)SecurityUtils.getSubject().getPrincipal();
        if(matterId==null){
            matterId=user.getId();
        }
        if(!matterId.equals(user.getId())){
            if(matterService.getById(matterId)==null){
                return ApiResult.ERROR("文件不存在");
            }
        }
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.View);
        MatterDTO parentMatter=matterService.getMatterDtoById(matterId,user.getId());
        return ApiResult.SUCCESS(parentMatter);
    }
    @PostMapping("/matter/add")
    @ApiOperation(value = "上传文件")
    public ApiResult upload(@RequestParam(value = "file") MultipartFile multipartFile,
                            @RequestParam(required = false,value = "matterId") String parentMatterId,
                            @RequestParam Boolean extendSuper) {
        try{
            MatterDTO matterDTO=matterService.uploadFile(multipartFile,parentMatterId);
            // 简单修复 extendSuper传入的字段
            matterService.update(new LambdaUpdateWrapper<Matter>().eq(Matter::getId,matterDTO.getId()).set(Matter::getExtendSuper,extendSuper));
            matterDTO.setExtendSuper(extendSuper);
            return ApiResult.SUCCESS(matterDTO);
        } catch (MinioException e) {
            log.error(e.getMessage());
            return ApiResult.ERROR("保存文件失败");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e.getMessage());
            return ApiResult.ERROR("IOException"+e.getMessage());
        }
    }
    @PostMapping("/matter/create")
    @ApiOperation(value = "使用模板创建文件文件")
    public ApiResult create(@RequestBody CreateDto createDto) {
        try{
            MatterDTO matterDTO=matterService.create(createDto);
            return ApiResult.SUCCESS(matterDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ApiResult.ERROR("创建失败，可能是存储服务器出错");
        }
    }
    @ApiOperation(value = "文件下载")
    @RequestMapping("/matter/download")
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
    @ApiOperation(value = "下载文件")
    @GetMapping ("/matter/downloadByToken/{token}")
    public ApiResult downloadByToken(@PathVariable String token,HttpServletResponse res) throws MinioException, IOException {
        try {
            Claims claims = JwtUtil.parseJWT(token);
            String objectName = claims.get("objectName").toString();
            String fileName = claims.get("fileName").toString();
            minioUtil.download(objectName,res, fileName);
        } catch (JwtException e) {
            return ApiResult.ERROR("链接过期");
        }
        return ApiResult.SUCCESS(token);
    }
    @ApiOperation(value = "临时访问地址")
    @GetMapping ("/matter/getTempAccessToken")
    public ApiResult getTempAccessToken(@RequestParam String matterId, @RequestParam(required = false) String version, HttpServletResponse res) throws MinioException, IOException {
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
        Map<String,Object> map=new HashMap<>();
        map.put("objectName",fileHistory.getObjectName());
        map.put("fileName",matter.getName());
        String token= JwtUtil.createJWT(map, (long) (24*60*60));
        return ApiResult.SUCCESS(token);
    }
    @ApiOperation(value = "文件删除")
    @DeleteMapping("/matter/delete")
    public ApiResult delete(@RequestParam String matterId) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        // 检查是否含有文件或子文件夹
        if (matterService.count(new LambdaQueryWrapper<Matter>().eq(Matter::getParentId,matterId))>0) {
            return ApiResult.ERROR("文件夹内含有文件，删除失败");
        }
        if(matterService.deleteMatter(matterId)){
            return ApiResult.SUCCESS("删除成功");
        }else{
            return ApiResult.ERROR("删除失败");
        }
    }
    @ApiOperation(value = "批量删除")
    @PostMapping("/matter/deletes")
    public ApiResult deletes(@RequestBody Map map) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        String matterIds= (String) map.get("matterIds");
        List<String> successList = matterService.deleteMatters(matterIds);
        return ApiResult.SUCCESS(successList);
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
    @ApiOperation("移动文件")
    @PostMapping("/matter/move")
    public ApiResult move(@RequestBody Map<String,Object> map){
        String target= (String) map.get("target");
        String matterIds= (String) map.get("matterIds");
        List<String> ids= List.of(matterIds.split(","));
        Matter targetMatter=matterService.getById(target);
        List<String> successList=new ArrayList<>();
        if(targetMatter==null){
            return ApiResult.ERROR("目标文件夹不存在");
        }
        for (String id : ids) {
            if(matterService.move(id, target)){
                successList.add(id);
            }
        }
        return ApiResult.SUCCESS(successList);
    }

    @ApiOperation(value = "请求历史版本列表")
    @GetMapping("/matter/version")
    public ApiResult<List<FileHistory>> matterVersion(@RequestParam String matterId){
        matterPermissionsService.checkMatterPermission(matterId, ActionEnum.View);
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId, matterId));
        List<FileHistory> fileHistories = fileHistoryService.list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId, matterId));
        return ApiResult.SUCCESS(fileHistories);
    }
    @ApiOperation(value = "请求TreeViewNode")
    @GetMapping("/matter/tree")
    public ApiResult tree(){
        User user =(User) SecurityUtils.getSubject().getPrincipal();
//        MatterDTO matterDTO=matterService.getTree("root", user.getId());
        MatterDTO matterDTO=matterService.getTreeV2(user.getId(), 0);
        return ApiResult.SUCCESS(matterDTO);
    }
    @ApiOperation(value = "搜索")
    @PostMapping("/matter/search")
    public ApiResult search(@RequestBody MatterSearchDTO dto){
        Page<MatterDTO> matterDTOPage = matterService.listSearch(Page.of(dto.getCurrent(), dto.getPageSize()), dto.getName());
        return ApiResult.SUCCESS(matterDTOPage);
    }
}
