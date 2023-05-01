package com.shih.icecms.controller;

import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.ApiResult;
import com.shih.icecms.utils.MinioUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "文件相关接口")
@Slf4j
@RestController
public class FileController {
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinioConfig prop;
    @GetMapping("/hello")
    public ApiResult Hello(){

        return ApiResult.SUCCESS("hello");
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

    @ApiOperation(value = "文件下载")
    @GetMapping("/download")
    public ResponseBody download(@RequestParam("fileName") String fileName, HttpServletResponse res) {
        minioUtil.download(fileName,res);
        return R.ok();
    }

    @ApiOperation(value = "删除文件", notes = "根据url地址删除文件")
    @PostMapping("/delete")
    public ResponseBody remove(String url) {
        String objName = url.substring(url.lastIndexOf(prop.getBucketName()+"/") + prop.getBucketName().length()+1);
        minioUtil.remove(objName);
        return R.ok().put("objName",objName);
    }*/

}
