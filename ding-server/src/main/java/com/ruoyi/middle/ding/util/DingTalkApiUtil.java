package com.ruoyi.middle.ding.util;

import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponseBody;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.ruoyi.middle.ding.config.DingTalkConfig;

@Component
public class DingTalkApiUtil
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkApiUtil.class);

    @Autowired
    private DingTalkConfig dingTalkConfig;

    private String accessToken;
    private long tokenExpireTime = 0;

    /**
     * 创建钉钉连接2.1
     * @return
     * @throws Exception
     */
    private Client createDingClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }

    /**
     * 创建钉钉连接1.0
     * @return
     * @throws Exception
     */
    private com.aliyun.dingtalkrobot_1_0.Client createDingClient_1() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkrobot_1_0.Client(config);
    }

    /**
     * 获取钉钉接口Token
     * @return
     * @throws Exception
     */
    private String getToken() throws Exception {
        Client client = createDingClient();
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest()
                .setAppKey(dingTalkConfig.getAppId())
                .setAppSecret(dingTalkConfig.getAppSecret());
        try {
            GetAccessTokenResponse accessToken = client.getAccessToken(getAccessTokenRequest);
            GetAccessTokenResponseBody body = accessToken.getBody();
            log.info("获取钉钉接口Token => {}", body.getAccessToken());
            return body.getAccessToken();
        } catch (Exception _err) {
            throw _err;
        }
    }

    // 通过临时授权码获取授权用户的个人信息
    public OapiV2UserGetuserinfoResponse getUserInfoByCode(String code) throws Exception {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getuserinfo");
        OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
        req.setCode(code);
        return client.execute(req, getToken());
    }

    // 根据unionId获取userid
    public OapiUserGetbyunionidResponse getUserByUnionId(String unionId) throws Exception {
        DingTalkClient clientDingTalkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/getbyunionid");
        OapiUserGetbyunionidRequest reqGetbyunionidRequest = new OapiUserGetbyunionidRequest();
        reqGetbyunionidRequest.setUnionid(unionId);
        return clientDingTalkClient.execute(reqGetbyunionidRequest, getToken());
    }

    //根据userId获取用户信息
    public OapiV2UserGetResponse getUserByUserId(String userId) throws Exception {
        DingTalkClient clientDingTalkClient2 = new DefaultDingTalkClient(
                "https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest reqGetRequest = new OapiV2UserGetRequest();
        reqGetRequest.setUserid(userId);
        reqGetRequest.setLanguage(dingTalkConfig.getLanguage());
        return clientDingTalkClient2.execute(reqGetRequest, getToken());
    }

    //根据corpId获取企业信息
    public OapiServiceGetAuthInfoResponse getCorpByCorpId(String corpId) throws Exception {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/service/get_auth_info");
        OapiServiceGetAuthInfoRequest req = new OapiServiceGetAuthInfoRequest();
//        req.setSuiteKey(dingTalkConfig.getSuiteKey());
        req.setAuthCorpid(corpId);
//        return client.execute(req, dingTalkConfig.getAppId(),dingTalkConfig.getAppSecret(),dingTalkConfig.getSuiteTicket());
        return client.execute(req, dingTalkConfig.getAppId(),dingTalkConfig.getAppSecret());
    }

}
