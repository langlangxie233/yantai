package com.cmsr.hik.vision.model.luansheng;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/7/2 10:55
 */
@Setter
@Getter
public class EmergencyDrillDto {

    private String id;
    private String planCode;
    private String exerciseName;
    private String exerciseType;
    private String exercisePurpose;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime drillEndTime;
    private String number;
    private String address;
    private String exerciseProcess;
    private String exerciseSummary;
    private String evalReason;
    private String delFlag = "0";

}
