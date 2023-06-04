package com.shih.icecms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.utils.MinioUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Api(tags = "文件相关接口")
@Slf4j
@RestController
public class FileController {
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinioConfig prop;

    @Resource
    MatterService matterService;
    @Resource
    HttpServletRequest httpServletRequest;
    @ApiOperation(value = "生成临时访问地址")
    @PostMapping("/shared")
    public ApiResult GetTemporaryAccessUrl(String fileName){
        String sharedUrl = minioUtil.GetTemporaryAccessUrl(fileName,60*60);
        return ApiResult.SUCCESS(sharedUrl);
    }
    @PostMapping("/upload")
    @ApiOperation(value = "上传文件")
    public ApiResult upload(@RequestParam(value = "file") MultipartFile multipartFile, @RequestParam(required = false) String fileId, @RequestParam String folderId,@RequestParam Integer fileType) {
        Matter matter =null;
        if(StringUtils.hasText(fileId)){
            matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getId,fileId).eq(Matter::getType,fileType).orderBy(true,false, Matter::getVersion).last("limit 1"));
        }
        if(matter !=null){
            matter.setVersion(matter.getVersion()+1);
            matter.setId(null);
            matter.setCreateTime(new Date().getTime());
            String objectName= matter.getId()+"/"+ matter.getVersion()+multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
            matter.setPath(objectName);
            minioUtil.upload(multipartFile, matter.getPath());
            matterService.save(matter);
        }else{
            String objectName= matter.getId()+"/"+ matter.getVersion()+multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
            matter.setPath(objectName);
            minioUtil.upload(multipartFile,objectName);
            matterService.save(matter);
        }
        return ApiResult.SUCCESS(matter.getId());
    }

    @ApiOperation(value = "创建文件夹")
    @PostMapping("/folder")
    public ResponseEntity add(@RequestBody MatterDTO fileDTO){

        return null;
    }



    @ApiOperation(value = "文件下载")
    @GetMapping("/download")
    public void download(@RequestParam String fileId, @RequestParam(defaultValue = "1") String fileVersion,@RequestParam(required = false) Integer fileType,HttpServletResponse res) throws MinioException, IOException {
        Matter matter = matterService.getOne(new LambdaQueryWrapper<Matter>().eq(Matter::getFileId,fileId)
                .eq(Matter::getVersion,fileVersion).eq(fileType!=null, Matter::getType,fileType));
        minioUtil.download(matter.getPath(),res, matter.getName());
    }
    @ApiOperation(value = "文件下载")
    @GetMapping("/downloadByObjectName")
    public void downloadByObjectName(@RequestParam String objectName, HttpServletResponse res) throws MinioException, IOException {
        minioUtil.download(objectName,res,"changes.zip");
    }
/*
    @ApiOperation(value = "查看存储bucket是否存在")
    @GetMapping("/bucketExists")
    public ResponseEntity bucketExists(@RequestParam("bucketName") String bucketName) {
        return ResponseEntity.ok().put("bucketName",minioUtil.bucketExists(bucketName));
    }

    @ApiOperation(value = "创建存储bucket")
    @GetMapping("/makeBucket")
    public ResponseBody makeBucket(String bucketName) {
        return R.ok().put("bucketName",minioUtil.makeBucket(bucketName));
    }

    @ApiOperation(value = "删除存储bucket")
    @GetMapping("/removeBucket")
    public ResponseBody removeBucket(String bucketName) {
        return R.ok().put("bucketName",minioUtil.removeBucket(bucketName));
    }

    @ApiOperation(value = "获取全部bucket")
    @GetMapping("/getAllBuckets")
    public ResponseBody getAllBuckets() {
        List<Bucket> allBuckets = minioUtil.getAllBuckets();
        return R.ok().put("allBuckets",allBuckets);
    }

    @ApiOperation(value = "文件上传返回url")
    @PostMapping("/upload")
    public ResponseBody upload(@RequestParam("file") MultipartFile file) {
        String objectName = minioUtil.upload(file);
        if (null != objectName) {
            return R.ok().put("url",(prop.getEndpoint() + "/" + prop.getBucketName() + "/" + objectName));
        }
        return R.error();
    }

    @ApiOperation(value = "图片/视频预览")
    @GetMapping("/preview")
    public ResponseBody preview(@RequestParam("fileName") String fileName) {
        return R.ok().put("filleName",minioUtil.preview(fileName));
    }



    @ApiOperation(value = "删除文件", notes = "根据url地址删除文件")
    @PostMapping("/delete")
    public ResponseBody remove(String url) {
        String objName = url.substring(url.lastIndexOf(prop.getBucketName()+"/") + prop.getBucketName().length()+1);
        minioUtil.remove(objName);
        return R.ok().put("objName",objName);
    }*/

}
