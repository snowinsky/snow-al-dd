package com.snow.al.dd.core.biztime.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "vendor_biz_time")
public class VendorBizTime {
    @Id
    private String id;
    @Indexed(unique = true, name = "vendorCode_uk")
    private String vendorCode;

    private List<BankBizTime> bankBizTimeList;


    @Data
    public static class BankBizTime {
        private String bankCode;
        /**
         * 1: 启用
         * 0: 禁用
         */
        private Integer maintenanceTimeIgnored;
        /**
         * yyyy-MM-dd HH:mm:ss
         * 2020/07/27 10:00:00~2020/07/27 12:00:00,2020/07/27 01:00:00~2020/07/27 14:00:00
         */
        private String maintenanceTime;
        /**
         * 09:00~12:00,14:00~17:00
         */
        private String bizTime;
    }
}
