# Download Minio Server  
https://www.minio.org.cn/download.shtml  
下载合适的版本

# windows
在windows上提供以服务的方式部署
首先修改xml配置文件  
更多配置详见[winsw项目](https://github.com/winsw/winsw/blob/v3/docs/xml-config-file.md)
```xml
<?xml version="1.0" encoding="GB2312" ?>
<service>
  <!-- 该服务的唯一标识 -->
  <id>Minio Service</id>
  <!-- 注册为系统服务的名称 -->
  <name>Minio Service</name>
  <!-- 对服务的描述 -->
  <description>Minio对象存储服务</description>
  <!-- 运行路径 -->
  <workingdirectory>E:/minio</workingdirectory>
  <!-- 程序路径 -->
  <executable>E:/minio/minio.exe</executable>
  <!-- 执行的参数     文件保存位置             certs证书保存位置            控制台访问地址            API访问地址    -->
  <arguments>server E:\minio\data --certs-dir E:\minio\config\certs --console-address ":9090" --address ":9000"</arguments>
  <!-- 日志保存地址 -->
  <logpath>E:\minio\logs</logpath>
  <log mode="roll-by-size">
    <!-- 一个日志文件大小，单位是k-->
    <sizeThreshold>60</sizeThreshold>
    <keepFiles>3</keepFiles>
  </log>
</service>
```
主要有`workingdirectory` `executable` `arguments`  
然后使用以下命令进行部署
### 安装
```shell
.\minio-service.exe install
```
### 移除服务
```shell
.\minio-service.exe uninstall
```
### 重启
```shell
.\minio-service.exe restart
```
### 停止
```shell
.\minio-service.exe stop
```
