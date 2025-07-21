package com.snow.al.dd.core.ddauth.vendor;

public class BankAcctSignAuthRequest extends AuthRequest<BankAcctSignAuthResponse>{

    @Override
    public Class<BankAcctSignAuthResponse> getResponseClass() {
        return BankAcctSignAuthResponse.class;
    }
}
