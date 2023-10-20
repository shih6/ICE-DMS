# windows
在windows上提供以服务的方式部署  
首先修改xml配置文件
```xml
<?xml version="1.0" encoding="GB2312" ?>
<service>
  <!-- 该服务的唯一标识 -->
  <id>ICEDMSService</id>
  <!-- 注册为系统服务的名称 -->
  <name>ICE DMS Service</name>
  <!-- 对服务的描述 -->
  <description>ICEDMS后端服务</description>
  <workingdirectory>E:/icedms</workingdirectory>
  <!-- 将java程序添加到系统服务 -->
  <executable>D:/wwwroot/jdk-11.0.16.1+1/bin/java.exe</executable>
  <!-- 执行的参数 -->
  <arguments>-jar -Dspring.config.location=E:/wwwroot/icedms/application.yml -Xmx1024M -Xms256M  E:/wwwroot/icedms/ice-dms.jar --server.port=8081</arguments>
  <log mode="none">
  </log>
</service>
```
主要有`workingdirectory` `executable` `arguments`  
然后使用以下命令进行部署
### 安装
```shell
.\icedms-service.exe install
```
### 移除服务
```shell
.\icedms-service.exe uninstall
```
### 重启
```shell
.\icedms-service.exe restart
```
### 停止
```shell
.\icedms-service.exe stop
```
