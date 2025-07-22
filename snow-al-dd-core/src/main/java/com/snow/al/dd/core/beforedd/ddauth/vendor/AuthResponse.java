package com.snow.al.dd.core.beforedd.ddauth.vendor;

public abstract class AuthResponse {
    protected String returnCode;
    protected String returnMsg;
    protected String returnCodeGroup;
    protected String returnCodeGroupDesc;

    protected VendorAuthResponseStatus authResponseStatus;
}
