package com.cmsr.hik.vision.model;

import lombok.Data;

@Data
public class ParkingHisRecord {
    private String crossRecordSyscode;
    private String parkSyscode;
    private String parkName;
    private String entranceSyscode;
    private String entranceName;
    private String roadwaySyscode;
    private String roadwayName;
    private String vehicleOut;
    private String releaseMode;
    private String releaseResult;
    private String releaseWay;
    private String releaseReason;
    private String plateNo;
    private String cardNo;
    private String vehicleColor;
    private String vehicleType;
    private String plateColor;
    private String plateType;
    private String carCategory;
    private String carCategoryName;
    private String vehiclePicUri;
    private String plateNoPicUri;
    private String facePicUri;
    private String aswSyscode;
    private String crossTime;
    private String createTime;
}
