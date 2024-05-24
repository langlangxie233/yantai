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
public class SecSpecialJobTicketApprovalDto {
    private String id;
    private String ticketId;
    private String processNodes;
    private String processPersonnel;
    private String processOpinion;
    private String processTime;
    private String signalImage;
    private String deleted;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createDate;
    private String createBy = "system";
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime updateDate;
    private String updateBy = "system";

}
