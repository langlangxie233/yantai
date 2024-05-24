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
public class SecSpecialJobTicketPowerDto {
    private String id;
    private String companyCode;
    private String ticketNo;
    private String ticketStatus;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime issueTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime planStartTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime planEndTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime endTime;
    private String ticketPosition;
    private String workAreaCode;
    private String ticketContent;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime checkedTime;
    private String checkedPerson;
    private String longitude;
    private String latitude;
    private String supervisorName;
    private String workerName;
    private String workDeptment;
    private String isContractorWork;
    private String contractorOrg;
    private String isAssociation;
    private String associationTicket;
    private String riskIdentification;
    private String disclosePerson;
    private String acceptPerson;
    private String tickerResponsName;
    private String majorPersonMobile;
    private String isChanged;
    private String changedCause;
    private String isCancelled;
    private String cancelledCause;
    private String mobileDeviceCode;
    private String workTicketAtt;
    private String safeDiscloseAtt;
    private String countersignImg;
    private String gasAnalysis;
    private String commitmentLetter;
    private String powerStrategy;
    private String workVoltage;
    private String electricalEquipment;
    private String headCode;
    private String personCode;
    private String deleted;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createDate;
    private String createBy;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime updateDate;
    private String updateBy;
    private String ticket_level;

}
