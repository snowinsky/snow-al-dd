package com.snow.al.dd.core.beforedd.ddauth.vendor;

public class ClientPreAuthRequest extends AuthRequest<ClientPreAuthResponse> {

    @Override
    public Class<ClientPreAuthResponse> getResponseClass() {
        return ClientPreAuthResponse.class;
    }
}
