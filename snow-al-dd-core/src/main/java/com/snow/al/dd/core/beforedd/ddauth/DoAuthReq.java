package com.snow.al.dd.core.beforedd.ddauth;

public class DoAuthReq {

    String merchantTradeNo;
    String clientId;


    public static class PreAuth extends DoAuthReq {
        String bankAcctNo;
        String bankAcctName;
        String bankAcctIdCard;
        String bankAcctPhone;
    }

    public static class SignAuth extends DoAuthReq {
        String vendorUniqueCode;
        String smsCode;
    }

    public static class ClientAuth extends DoAuthReq {
        String clientIdCard;
        String clientPhone;
    }
}
