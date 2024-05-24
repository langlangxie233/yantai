package com.cmsr.hik.vision.model.silu.specialassignments;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSpecialJobTicketGasAnalysisDto {
    private String id;
    private String ticketID;
    private String ticketType;
    private String gasType;
    private String gasName;
    private String analysisResults;
    private String resultsUnit;
    private String analyst;
    private String analystTime;
    private String analystPart;
    private String deleted;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createDate;
    private String createBy;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime updateDate;
    private String updateBy;

}
