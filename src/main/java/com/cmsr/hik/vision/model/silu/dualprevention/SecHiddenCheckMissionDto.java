package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报隐患排查任务数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecHiddenCheckMissionDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 管控措施主键 ID，在上报时会校验该ID是否存在，请先上传安全风险管控措施数据
     */
    private String riskMeasureId;
    /**
     * 隐患排查内容
     */
    private String troubleshootContent;
    /**
     * 排查周期，本次生成排查任务到下次生成排查任务的时间跨度，需大于0
     */
    private Integer checkCycle;
    /**
     * 排查周期单位（小时、天、月、年），本次生成排查任务到下次生成排查任务的时间跨度的单位
     */
    private String checkCycleUnit;
    /**
     * 任务开始时间：指该任务首次开始执行的时间，时间格式yyyyMMddHHmmss
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime taskStartTime;
    /**
     * 工作开始时间：当排查周期是小时的时候为必填，时间格式例如：8:00:00，要求其他巡检周期单位无需填写
     */
    private String workStartTime;
    /**
     * 工作结束时间
     */
    private String workEndTime;
    /**
     * 工作日类型（每天：0；法定工作日（除法定放假以为的所有）：1；非法定工作日（除了放假的时候）：2）
     */
    private String workDayType;
    /**
     * 任务类型(日常任务：0；主要负责人任务：1；技术负责人任 务：2；操作负责人任务：3；)默认为日常任务：0
     */
    private String workType;
    /**
     * 包保任务对应项：当任务类型为主要负责人任务、技术负责人任务、操作负责人任务时，此项为必填。数字应与《危险化学品企业重大危险源安全包保责任人隐患排查任务清单》各项标号对应。主要负责人：1-9技术负责人：1-9 操作负责人：1- 10，填写1-10的数值编码
     */
    private String taskNum;
    /**
     * 删除状态:正常：0；已删除：1
     */
    private String deleted;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime createDate;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 更新时间（新创建的数据更新时间和创建时间相同）
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime updateDate;
    /**
     * 更新人
     */
    private String updateBy;

}
