package com.snow.al.dd.core.biztime.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class VendorInfo {

    /**
     * code : CCB(Direct Linkage)
     * name : CCB
     * maxAmountLimitPerDay : 10000000
     * supportedBanks : [{"bankCode":"CCB","bankName":"CCB","bankNo":"100","bankDescription":"","businessTime":"09:00~12:00,14:00~17:00","maintenanceStatus":1,"cutoffTime":"19:00","maintenanceTime":"2020/07/27 10:00:00~2020/07/27 12:00:00,2020/07/27 01:00:00~2020/07/27 14:00:00","maintenanceDelayTime":60,"maxSendTimes":5,"authRequired":"y","minAmountLimitByTransaction":500,"maxAmountLimitByTransaction":5000,"amountLimitByAccount":50000,"costType":1,"costFormula":"","shortfallSupported":"n"}]
     */

    private String code;
    private String name;
    private List<SupportedBanksBean> supportedBanks;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SupportedBanksBean> getSupportedBanks() {
        return supportedBanks;
    }

    public void setSupportedBanks(List<SupportedBanksBean> supportedBanks) {
        this.supportedBanks = supportedBanks;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class SupportedBanksBean {
        /**
         * bankCode : CCB
         * bankName : CCB
         * bankNo : 100
         * bankDescription :
         * businessTime : 09:00~12:00,14:00~17:00
         * maintenanceStatus : 1
         * cutoffTime : 19:00
         * maintenanceTime : 2020/07/27 10:00:00~2020/07/27 12:00:00,2020/07/27 01:00:00~2020/07/27 14:00:00
         * maintenanceDelayTime : 60
         * maxSendTimes : 5
         * authRequired : y
         * minAmountLimitByTransaction : 500
         * maxAmountLimitByTransaction : 5000
         * amountLimitByAccount : 50000
         * costType : 1
         * costFormula :
         * shortfallSupported : n
         */

        private String bankCode;
        private String bankName;
        private String bankNo;
        private String businessTime;
        private int maintenanceStatus;
        private String cutoffTime;
        private String maintenanceTime;
        private int maintenanceDelayTime;
        private int maxSendTimes;
        private String authRequired;
        private int costType;
        private String costFormula;
        private String shortfallSupported;

        public String getBankCode() {
            return bankCode;
        }

        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankNo() {
            return bankNo;
        }

        public void setBankNo(String bankNo) {
            this.bankNo = bankNo;
        }

        public String getBusinessTime() {
            return businessTime;
        }

        public void setBusinessTime(String businessTime) {
            this.businessTime = businessTime;
        }

        public int getMaintenanceStatus() {
            return maintenanceStatus;
        }

        public void setMaintenanceStatus(int maintenanceStatus) {
            this.maintenanceStatus = maintenanceStatus;
        }

        public String getCutoffTime() {
            return cutoffTime;
        }

        public void setCutoffTime(String cutoffTime) {
            this.cutoffTime = cutoffTime;
        }

        public String getMaintenanceTime() {
            return maintenanceTime;
        }

        public void setMaintenanceTime(String maintenanceTime) {
            this.maintenanceTime = maintenanceTime;
        }

        public int getMaintenanceDelayTime() {
            return maintenanceDelayTime;
        }

        public void setMaintenanceDelayTime(int maintenanceDelayTime) {
            this.maintenanceDelayTime = maintenanceDelayTime;
        }

        public int getMaxSendTimes() {
            return maxSendTimes;
        }

        public void setMaxSendTimes(int maxSendTimes) {
            this.maxSendTimes = maxSendTimes;
        }

        public String getAuthRequired() {
            return authRequired;
        }

        public void setAuthRequired(String authRequired) {
            this.authRequired = authRequired;
        }

        public int getCostType() {
            return costType;
        }

        public void setCostType(int costType) {
            this.costType = costType;
        }

        public String getCostFormula() {
            return costFormula;
        }

        public void setCostFormula(String costFormula) {
            this.costFormula = costFormula;
        }

        public String getShortfallSupported() {
            return shortfallSupported;
        }

        public void setShortfallSupported(String shortfallSupported) {
            this.shortfallSupported = shortfallSupported;
        }
    }
}
