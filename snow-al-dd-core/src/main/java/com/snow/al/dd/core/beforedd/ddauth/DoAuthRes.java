package com.snow.al.dd.core.beforedd.ddauth;

import java.time.Instant;

public class DoAuthRes {
    String returnCode;
    String returnCodeDesc;

    public static class PreAuth extends DoAuthRes {
        String vendorUniqueCode;
    }

    public static class SignAuth extends DoAuthRes {
        String authCode;
        Instant validTo;
    }

    public static class ClientAuth extends DoAuthRes {

    }
}
