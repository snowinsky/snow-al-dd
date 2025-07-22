package com.snow.al.dd.core.beforedd.ddauth.state;

public interface DdAuthState {

    void doAuth(DdAuthContext ddAuthContext);

    void setState(DdAuthState nextState);
}
