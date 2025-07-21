package com.snow.al.dd.core.ddauth.vendor;

public class BankAcctPreAuthRequest extends AuthRequest<BankAcctPreAuthResponse> {

    @Override
    public Class<BankAcctPreAuthResponse> getResponseClass() {
        return BankAcctPreAuthResponse.class;
    }
}
