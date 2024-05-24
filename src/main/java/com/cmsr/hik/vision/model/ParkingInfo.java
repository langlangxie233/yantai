package com.cmsr.hik.vision.model;

import lombok.Data;

/**
 * https://open.hikvision.com/docs/docId?productId=5c67f1e2f05948198c909700&version=%2Ff95e951cefc54578b523d1738f65f0a1&tagPath=API%E5%88%97%E8%A1%A8-%E8%BD%A6%E8%BE%86%E7%AE%A1%E6%8E%A7-%E5%81%9C%E8%BD%A6%E5%9C%BA%E5%8A%9F%E8%83%BD#c4292e21
 * @author xiayuanming
 * @since 2024年4月20日09点59分
 */
@Data
public class ParkingInfo {
    private String inRecordSyscode;
    private String vehiclePicUri;
    private String cardNo;
    private String inTime;
    private String parkTime;
    private String parkSyscode;
    private String parkName;
    private String plateNoPicUri;
    private String aswSyscode;
    private String plateNo;
}
