package com.cmsr.hik.vision.model.silu.personnelpositioning;

import lombok.Getter;
import lombok.Setter;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecEmployeeAlarmDataDto {
    private String thirdId;
    private String alarmType;
    private String alarmTime;
    private String locationCode;
    private String longitude;
    private String latitude;
    private String address;
    private String alarmReason;
    private String alarmSource;
    private String alarmStatus;
    private String image;
}
