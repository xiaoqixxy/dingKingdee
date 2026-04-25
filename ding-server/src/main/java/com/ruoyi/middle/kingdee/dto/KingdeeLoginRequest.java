package com.ruoyi.middle.kingdee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 金蝶云星空登录请求参数DTO
 * <p>
 * 用于接收前端或第三方系统传递的金蝶登录信息。
 * 使用 @JsonProperty 注解映射前端全大写的字段名（如SERVER_URL、CID等）。
 * <p>
 * 示例请求JSON：
 * <pre>
 * {
 *   "SERVER_URL": "http://192.168.1.1/k3cloud",
 *   "CID": "6882df0b41e0a4",
 *   "USER_NAME": "admin",
 *   "APP_ID": "285402_xxxxx",
 *   "APP_SECRET": "2f9880a4956c42af8df761c6039f803d"
 * }
 * </pre>
 *
 * @author ruoyi
 */
@Data
public class KingdeeLoginRequest {

    /**
     * 金蝶服务器地址
     * <p>
     * 格式：http://ip:端口/k3cloud
     * 例如：http://192.168.1.1:8090/k3cloud
     */
    @JsonProperty("SERVER_URL")
    private String SERVER_URL;

    /**
     * 账套ID
     * <p>
     * 金蝶系统中账套的唯一标识符
     */
    @JsonProperty("CID")
    private String CID;

    /**
     * 用户名
     * <p>
     * 金蝶系统中的登录用户名
     */
    @JsonProperty("USER_NAME")
    private String USER_NAME;

    /**
     * 应用ID
     * <p>
     * 第三方应用授权ID，用于API调用身份验证
     */
    @JsonProperty("APP_ID")
    private String APP_ID;

    /**
     * 应用密钥
     * <p>
     * 第三方应用授权密钥，与APP_ID配对使用
     */
    @JsonProperty("APP_SECRET")
    private String APP_SECRET;

    /**
     * 语言ID
     * <p>
     * 指定返回数据的语言类型，默认2052（简体中文）
     * <ul>
     *   <li>2052 - 简体中文</li>
     *   <li>1033 - 英文</li>
     *   <li>1028 - 繁体中文</li>
     * </ul>
     */
    private Integer lcid = 2052;
}
