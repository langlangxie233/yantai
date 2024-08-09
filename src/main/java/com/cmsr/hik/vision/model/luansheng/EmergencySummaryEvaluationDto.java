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
public class EmergencySummaryEvaluationDto {
    /**
     * id
     */
    private String id;
    /**
     * 应急预案id
     */
    private String planId;
    /**
     * 事故编号
     */
    private String accidentId;
    /**
     * 事发单位id
     */
    private String companyId;
    /**
     * 事发单位名称
     */
    private String companyName;
    /**
     * 事发时间
     */
    private String incidentTime;
    /**
     * 事发地点
     */
    private String location;
    /**
     * 事发类型
     */
    private String incidentType;
    /**
     * 事故类型
     */
    private String accidentType;
    /**
     * 最近修改人
     */
    private String modifiedBy;
    /**
     * 先期处置情况
     */
    private String initialDisposalSituation;
    /**
     * 应急预案实施情况
     */
    private String emergencyStatus;
    /**
     * 组织指挥情况
     */
    private String organizationalCommandSituation;
    /**
     * 现场救援方案执行情况
     */
    private String implementationStatus;
    /**
     * 现场应急救援队伍工作情况
     */
    private String workSituation;
    /**
     * 现场管理和信息发布情况
     */
    private String disseminationSituation;
    /**
     * 应急资源保障情况
     */
    private String guaranteeSituation;
    /**
     * 救援成效、经验和教训
     */
    private String summary;
    /**
     * 报警人
     */
    private String alarmPerson;
    private String delFlag = "0";

}
