package com.dj.middle.payment.enums;

/** 支付渠道数据对象。 */
public enum PayChannel
{
    /** 支付宝支付渠道。 */
    ALIPAY("alipay", "支付宝"),
    /** 微信支付渠道。 */
    WECHAT_PAY("wechat_pay", "微信支付"),
    /** 钉钉支付渠道。 */
    DING_PAY("ding_pay", "钉钉支付");

    /** 编码。 */
    private final String code;
    /** 名称。 */
    private final String name;

    /** 初始化当前组件并保存构造参数。 */
    PayChannel(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    /** 读取编码。 */
    public String getCode()
    {
        return code;
    }

    /** 读取名称。 */
    public String getName()
    {
        return name;
    }

    /** 将渠道编码映射为受支持的支付渠道枚举。 */
    public static PayChannel fromCode(String code)
    {
        for (PayChannel channel : values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("未知支付渠道: " + code);
    }
}
