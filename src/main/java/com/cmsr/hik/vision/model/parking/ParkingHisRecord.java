package com.cmsr.hik.vision.model.parking;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParkingHisRecord {
    private String crossRecordSyscode;
    private String parkSyscode;
    private String parkName;
    private String entranceSyscode;
    private String entranceName;
    private String roadwaySyscode;
    private String roadwayName;
    private Integer vehicleOut;
    private Integer releaseMode;
    private Integer releaseResult;
    private Integer releaseWay;
    private Integer releaseReason;
    private String plateNo;
    private String cardNo;
    private Integer vehicleColor;
    private Integer vehicleType;
    private Integer plateColor;
    private Integer plateType;
    private String carCategory;
    private String carCategoryName;
    private String vehiclePicUri;
    private String plateNoPicUri;
    private String facePicUri;
    private String aswSyscode;
    private LocalDateTime crossTime;
    private LocalDateTime createTime;
}
