package com.snow.al.dd.core.beforedd.ddauth.vendor;

public enum VendorAuthResponseStatus  {
    /**
     * 成功，不返回authCode, 需要编一个假的放进去
     */
    SUCCESS_WITHOUT_AUTHCODE,
    /**
     * 成功，返回authCode，而且会覆盖以前的 authCode，需要把以前的所有的 authCode 都设置过期，然后再设置新的 authCode
     */
    SUCCESS_WITH_AUTHCODE_OVERRIDE,
    /**
     * 成功，返回authCode，而且不会覆盖以前的 authCode，直接插入就行
     */
    SUCCESS_WITH_AUTHCODE_APPEND,
    SUCCESS_WAIT_CALLBACK,
    /**
     * 失败，返回重复签约，则无需操作数据库，直接返回成功即可
     */
    FAIL_DUPLICATE_SIGN,
    /**
     * 失败，直接返回失败原因即可
     */
    FAIL_OTHER,
    /**
     * 失败，超时，可以提示客户重试，比后端记录超时次数，如果达到一定的阈值，需要报警联系银行看原因
     */
    FAIL_TIMEOUT;

}
