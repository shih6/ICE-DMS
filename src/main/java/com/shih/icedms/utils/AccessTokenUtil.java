package com.shih.icedms.utils;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.shih.icedms.config.DingTalkConfig;
import com.shih.icedms.entity.AccessToken;
import com.taobao.api.ApiException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AccessTokenUtil {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DingTalkConfig dingTalkConfig;
    public AccessToken getAccessToken(){
        Object accessToken;
        AccessToken as=new AccessToken();
        try {
            accessToken = redisTemplate.opsForValue().get("accessToken");
        }catch (RedisConnectionFailureException e){
            log.error(e.getMessage());
            throw e;
        }
        if(accessToken==null){
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest request = new OapiGettokenRequest();
            request.setAppkey(dingTalkConfig.getAppKey());
            request.setAppsecret(dingTalkConfig.getAppSecret());
            request.setHttpMethod("GET");
            OapiGettokenResponse response = null;
            try {
                response = client.execute(request);
            } catch (ApiException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            if(response.getAccessToken()==null){
                log.error(response.toString());
                throw new RuntimeException(response.getErrmsg());
            }
            as.setAccessToken(response.getAccessToken());
            redisTemplate.opsForValue().set("accessToken",response.getAccessToken(),response.getExpiresIn()-30*60L, TimeUnit.SECONDS);
            return as;
        }
        as.setAccessToken(accessToken.toString());
        return as;
    }
}
