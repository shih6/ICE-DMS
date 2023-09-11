package com.shih.icedms.controller;

import com.shih.icedms.dto.ApiResult;
import com.shih.icedms.entity.Setting;
import com.shih.icedms.service.SettingService;
import com.shih.icedms.service.UsersService;
import com.shih.icedms.utils.AccessTokenUtil;
import com.shih.icedms.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InstallController {
    @Autowired
    private UsersService usersService;
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private AccessTokenUtil accessTokenUtil;
    @Autowired
    private SettingService settingService;
    @GetMapping("/install/test")
    public ApiResult installTest(){
        //MybatisPlus测试
        Map<String,String> map=new HashMap<>();
        try {
            usersService.list();
            map.put("DataBase","SUCCESS");
        }catch (Exception e){
            map.put("DataBase",e.getMessage());
        }
        //Minio 存储服务器测试
        try {
            minioUtil.getAllBuckets();
            map.put("Minio","SUCCESS");
        }catch (Exception e){
            map.put("Minio",e.getMessage());
        }
        //钉钉 测试
        try {
            accessTokenUtil.getAccessToken();
            map.put("DingTalk Config","SUCCESS");
        }catch (Exception e){
            map.put("DingTalk Config",e.getMessage());
        }
        return ApiResult.SUCCESS(map);
    }
    @GetMapping("/setting/web")
    public ApiResult webSetting(){
        List<String> keyList=new ArrayList<>();
        keyList.add("DINGTALK_APP_KEY");
        keyList.add("LEARN_SYS_HREF");
        keyList.add("DOCUMENT_CALLBACK_HOST");
        keyList.add("DOCUMENT_SERVER_HOST");
        List<Setting> settingList=settingService.listByIds(keyList);
        return ApiResult.SUCCESS(settingList);

    }
}
