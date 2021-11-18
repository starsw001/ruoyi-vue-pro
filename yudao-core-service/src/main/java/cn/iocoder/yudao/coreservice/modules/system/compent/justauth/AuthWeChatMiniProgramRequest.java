package cn.iocoder.yudao.coreservice.modules.system.compent.justauth;

import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;

// TODO @timfruit：新建一个 yudao-spring-boot-starter-biz-social 包，把这个拓展拿进去哈。另外，可以思考下。
// 1. application-local.yaml 的配置里，justauth.extend.enum-class 能否不配置，而是自动配置好
// 2. application-local.yaml 的配置里，justauth.extend.extend.config.WECHAT_MINI_PROGRAM 有办法和 justauth.type.WECHAT_MP 持平
/**
 * 微信小程序登陆
 *
 * @author timfruit
 * @date 2021-10-29
 */
public class AuthWeChatMiniProgramRequest extends AuthDefaultRequest {

    public AuthWeChatMiniProgramRequest(AuthConfig config) {
        super(config, AuthExtendSource.WECHAT_MINI_PROGRAM);
    }

    public AuthWeChatMiniProgramRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthExtendSource.WECHAT_MINI_PROGRAM, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        // https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
        String response = new HttpUtils(config.getHttpConfig()).get(accessTokenUrl(authCallback.getCode()));
        JSONObject accessTokenObject = JSONObject.parseObject(response); // TODO @timfruit：使用 JsonUtils，项目尽量避免直接使用某个 json 库

        this.checkResponse(accessTokenObject);

        AuthExtendToken token = new AuthExtendToken();
        token.setMiniSessionKey(accessTokenObject.getString("session_key"));
        token.setOpenId(accessTokenObject.getString("openid"));
        token.setUnionId(accessTokenObject.getString("unionid"));
        return token;
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        // https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return AuthUser.builder()
                .uuid(authToken.getOpenId())
                //TODO 是使用默认值，还是有小程序获取用户信息 和 code 一起传过来
                .nickname("")
                .avatar("")
                .token(authToken)
                .source(source.toString())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        int code = object.getIntValue("errcode");
        if(code != 0){ // TODO @timfruit：if (code != 0) { ，注意空格
            throw new AuthException(object.getIntValue("errcode"), object.getString("errmsg"));
        }
    }

    /**
     * 返回获取 accessToken 的 url
     *
     * @param code 授权码
     * @return 返回获取 accessToken 的 url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(source.accessToken())
                .queryParam("appid", config.getClientId())
                .queryParam("secret", config.getClientSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build();
    }

}
