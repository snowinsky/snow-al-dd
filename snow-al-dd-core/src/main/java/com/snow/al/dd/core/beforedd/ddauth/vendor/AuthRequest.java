package com.snow.al.dd.core.beforedd.ddauth.vendor;

public abstract class AuthRequest<T extends AuthResponse> {
    public abstract Class<T> getResponseClass();
    protected String clientId;
}
