package com.cmsr.hik.vision.model.luansheng;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AlarmReceptionDto {

    private String id;
    private String accidentName = "手工填报事故";
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime alarmTime;
    private String accidentType;
    private String latitude;
    private String longitude;
    private String enterpriseName ;
    private String reportedName;
    private String accidentDescription;
    private String judgmentProcessRecord;
    private String delFlag = "0";

}
