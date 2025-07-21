package com.snow.al.dd.core.ddauth.vendor;

public interface VendorAuthAdapter {

    BankAcctPreAuthResponse bankAcctPreAuth(BankAcctSignAuthRequest preAuthRequest);

    BankAcctSignAuthResponse bankAcctSignAuth(BankAcctSignAuthRequest signAuthRequest);

    BankAcctSignAuthResponse convertBankAcctSignCallbackMsg(Object msg);

    ClientPreAuthResponse clientPreAuth(ClientPreAuthRequest preAuthRequest);

    ClientPreAuthResponse convertClientPreAuthCallbackMsg(Object msg);


}
