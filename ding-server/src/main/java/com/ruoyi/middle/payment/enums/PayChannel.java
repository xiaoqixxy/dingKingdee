package com.ruoyi.middle.payment.enums;

public enum PayChannel
{
    ALIPAY("alipay", "支付宝"),
    WECHAT_PAY("wechat_pay", "微信支付"),
    DING_PAY("ding_pay", "钉钉支付");

    private final String code;
    private final String name;

    PayChannel(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

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
