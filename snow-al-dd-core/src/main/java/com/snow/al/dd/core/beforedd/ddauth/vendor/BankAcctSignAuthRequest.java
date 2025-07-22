package com.snow.al.dd.core.beforedd.ddauth.vendor;

public class BankAcctSignAuthRequest extends AuthRequest<BankAcctSignAuthResponse>{

    @Override
    public Class<BankAcctSignAuthResponse> getResponseClass() {
        return BankAcctSignAuthResponse.class;
    }
}
