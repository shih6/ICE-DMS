package com.shih.icecms.service.impl;

import com.aliyun.dingtalkcontact_1_0.Client;
import com.aliyun.dingtalkcontact_1_0.models.GetUserHeaders;
import com.aliyun.dingtalkcontact_1_0.models.GetUserResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserGetbyunionidRequest;
import com.dingtalk.api.response.OapiUserGetbyunionidResponse;
import com.shih.icecms.config.DingTalkConfig;
import com.shih.icecms.entity.Users;
import com.shih.icecms.mapper.UsersMapper;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.AccessTokenUtil;
import com.taobao.api.ApiException;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 1
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2023-06-04 18:23:32
*/
@Service
@Log4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{
    @Resource
    DingTalkConfig dingTalkConfig;
    @Resource
    AccessTokenUtil accessTokenUtil;
    @Override
    public String getRId(String authCode) {
        log.info(String.format("StartW9pDGYqSpe <getUserIdByDingTalkAuthCode> <AuthCode:%s}>", authCode ));
        try {
            GetUserResponse rsp = this.getUserinfo(authCode);
            String unionId=rsp.getBody().getUnionId();
            return getRIdByUnionId(unionId);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException("登录失败");
        }finally {
            log.info("EndW9pDGYqSpe <getUserIdByDingTalkAuthCode>");
        }
    }
    public String getRIdByUnionId(String unionId){
        log.info(String.format("StartW9pDGYqSpe <getUserIdByUnionId> <unionId:%s}>", unionId));
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/getbyunionid");
            OapiUserGetbyunionidRequest req = new OapiUserGetbyunionidRequest();
            req.setUnionid(unionId);
            OapiUserGetbyunionidResponse rsp = client.execute(req, accessTokenUtil.getAccessToken().getAccessToken());
            return rsp.getResult().getUserid();
        }catch (ApiException e){
            log.error(e.getMessage(),e);
            throw new RuntimeException("登录失败");
        }catch (Exception e){
            throw e;
        }finally {
            log.info("EndW9pDGYqSpe <getUserIdByUnionId>");
        }
    }
    /**
     * 获取用户个人信息
     * @param
     * @return
     * @throws Exception
     */
    public GetUserResponse getUserinfo(String authCode) throws Exception {
        log.info(String.format("StartW9pDGYqSpe <getUserinfo> <AuthCode:%s}>", authCode));
        try{
            Client client = contactClient();
            GetUserHeaders getUserHeaders = new GetUserHeaders();
            getUserHeaders.xAcsDingtalkAccessToken = getDingtalkAccessToken(authCode);
            //获取用户个人信息，如需获取当前授权人的信息，unionId参数必须传me
            return client.getUserWithOptions("me", getUserHeaders, new RuntimeOptions());
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw e;
        }finally {
            log.info("EndW9pDGYqSpe <getUserinfo>");
        }
    }
    public String getDingtalkAccessToken(String authCode) throws Exception {
        com.aliyun.dingtalkoauth2_1_0.Client client = authClient();
        GetUserTokenRequest getUserTokenRequest = new GetUserTokenRequest().setClientId(dingTalkConfig.getAppKey()).setClientSecret(dingTalkConfig.getAppSecret()).setCode(authCode).setGrantType("authorization_code");
        GetUserTokenResponse getUserTokenResponse = client.getUserToken(getUserTokenRequest);
        return getUserTokenResponse.getBody().getAccessToken();
    }
    public static Client contactClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }
    public static com.aliyun.dingtalkoauth2_1_0.Client authClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }

}




