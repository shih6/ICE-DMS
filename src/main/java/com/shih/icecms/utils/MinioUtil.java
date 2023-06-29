package com.shih.icecms.utils;

import com.shih.icecms.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MinioUtil {
    @Autowired
    private MinioConfig prop;
    @Resource
    private HttpServletRequest request;
    @Resource
    private MinioClient minioClient;
    private static final String PATTERN = "^bytes=\\d*-\\d*(/\\d*)?(,\\d*-\\d*(/\\d*)?)*$";
    /**
     * 查看存储bucket是否存在
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }
    /**
     * 创建存储bucket
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 删除存储bucket
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 获取全部bucket
     */
    public List<Bucket> getAllBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return buckets;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 文件上传
     *
     * @param url 文件路径
     * @return Boolean
     */
    public String upload(URL url, String objectName){
        try {
            URLConnection urlConnection=url.openConnection();
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(prop.getBucketName()).object(objectName)
                    .stream(urlConnection.getInputStream(),urlConnection.getContentLength(),-1).contentType(urlConnection.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return objectName;
    }
    public String upload(MultipartFile file,String objectName) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(prop.getBucketName()).object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            ObjectWriteResponse response=minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return objectName;
    }
    /**
     * 预览图片
     * @param fileName
     * @return
     */
    public String preview(String fileName){
        // 查看文件地址
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder().bucket(prop.getBucketName()).object(fileName).method(Method.GET).build();
        try {
            String url = minioClient.getPresignedObjectUrl(build);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 文件下载
     * @param objectName 对象名
     * @param response response
     * @return Boolean
     */
    public void download(String objectName, HttpServletResponse response) throws MinioException, IOException {
        download(objectName, response,null);
    }
    /**
     * 文件下载
     * @param objectName 对象名
     * @param response response
     * @param fileName 自定义文件名
     * @return Boolean
     */
    public void download(String objectName, HttpServletResponse response,String fileName) throws MinioException, IOException {
        StatObjectResponse statObjectResponse;
        try{
            statObjectResponse=minioClient.statObject(StatObjectArgs.builder().bucket(prop.getBucketName()).object(objectName).build());
        }catch (Exception e){
            throw new MinioException(e.getMessage());
        }
        Long size =statObjectResponse.size();
        // 验证和解析Range请求头 -------------------------------------------------------------

        long start=0;
        long end=size-1;

        // Validate and process Range and If-Range headers.
        String range = request.getHeader("Range");
        if (range != null) {

            /*
             * 如果Range请求头不满足规范格式，那么发送错误请求
             * */
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches(PATTERN)) {
                response.setHeader("Content-Range", "bytes */" + size); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            /*
             * If-Range 头字段通常用于断点续传的下载过程中，用来自从上次中断后，确保下载的资源没有发生改变。
             * */
/*            String ifRange = request.getHeader("If-Range");
            if (ifRange != null && !ifRange.equals(md5)) {
                // 如果资源发生了改变，直接将数据全部返回
                ranges.add(full);
            }*/

            /*
             * 如果If-Range请求头是合法的，也就是视频数据并没有更新
             * 例子：bytes:10-80,bytes:80-180
             * */
            // If any valid If-Range header, then process each part of byte range.
                // substring去除bytes:
            for (String part : range.substring(6).split(",")) {
                // Assuming a file with size of 100, the following examples returns bytes at:
                // 50-80 (50 to 80), 40- (40 to size=100), -20 (size-20=80 to size=100).

                //去除多余空格
                part = part.trim();

                /*
                 * 解决20-80及20-80/60的切割问题
                 * */
                start = subLong(part, 0, part.indexOf("-"));
                int index1 = part.indexOf("/");
                int index2 = part.length();
                int index = index2 > index1 && index1 > 0 ? index1 : index2;
                end = subLong(part, part.indexOf("-") + 1, index);

                // 如果是-开头的情况 -20
                    if (start == -1) {
                        start = size - end;
                        end = size - 1;
                        // 如果是20但没有-的情况，或者end> size - 1的情况
                    } else if (end == -1 || end > size - 1) {
                        end = size - 1;
                    }

                    /*
                     * 如果范围不合法, 80-10
                     * */
                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader("Content-Range", "bytes */" + size); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }
            }
        }
        if((end-start+1)!=size){
            //断点传输下载视频返回206
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(prop.getBucketName())
                .object(objectName)
                .offset(start)
                .length(end-start+1)
                .build();

        response.setCharacterEncoding("utf-8");
        // 设置强制下载不打开
        response.setContentType("application/octet-stream");
        response.setHeader("Accept-Ranges","bytes");
        response.setHeader("Content-Length", String.valueOf(end-start+1));
        //Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + statObjectResponse.size());
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(StringUtils.isEmpty(fileName)?objectName:fileName, "utf-8"));
        try (GetObjectResponse objectResponse = minioClient.getObject(objectArgs)){
            byte[] buf = new byte[1024];
            int len;

            try (ServletOutputStream stream = response.getOutputStream()){
                while ((len=objectResponse.read(buf))!=-1){
                    stream.write(buf,0,len);
                }
                stream.close();
                stream.flush();
                objectResponse.close();
            }
        }catch (ClientAbortException clientAbortException){
            log.warn("用户停止下载");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 删除对象
     * @param objectName 对象名
     * @return Boolean
     */
    public void delete(String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        StatObjectResponse statObjectResponse;
        try{
            statObjectResponse=minioClient.statObject(StatObjectArgs.builder().bucket(prop.getBucketName()).object(objectName).build());
        }catch (Exception e){
            throw new MinioException(e.getMessage());
        }
        RemoveObjectArgs removeObjectArgs=RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(objectName).build();
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(prop.getBucketName())
                .object(objectName).build();
        minioClient.removeObject(removeObjectArgs);

    }
    /**
     * 查看文件对象
     * @return 存储bucket内文件对象信息
     */
    public List<Item> listObjects() {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(prop.getBucketName()).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }
    /**
     * 查看文件对象
     * @return 存储bucket内文件对象信息
     */
    public List<Item> listVersions(String objectName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(prop.getBucketName()).startAfter(objectName).includeVersions(true).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }
    /**
     * 删除
     * @param fileName
     * @return
     * @throws Exception
     */
    public boolean remove(String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(fileName).build());
        }catch (Exception e){
            return false;
        }
        return true;
    }
    /**
     * 获取临时授权地址
     * @param fileName 文件名(key)
     * @param expireTime second
     * @return
     * @throws Exception
     */
    public String GetTemporaryAccessUrl(String fileName,int expireTime){
        try {
            return minioClient.getPresignedObjectUrl( GetPresignedObjectUrlArgs.builder().bucket(prop.getBucketName()).expiry(expireTime).object(fileName).method(Method.GET).build());
        }catch (Exception e){
            throw new RuntimeException("获取零时地址失败");
        }
    }
    public static long subLong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }
}
