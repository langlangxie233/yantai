package com.cmsr.hik.vision.model.silu.dualprevention;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报隐患排查记录数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecHiddenCheckRecordDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 隐患排查任务ID
     */
    private String checkTaskId;
    /**
     * 时间格式yyyyMMddHHmmss
     */
    private LocalDateTime checkTime;
    /**
     * 风险分析对象所属部门负责人姓名
     */
    private String hazardLiablePerson;
    /**
     * 风险分析单元名称
     */
    private String riskUnitName;
    /**
     * 风险等级:0-重大风险，1-较大风险，2-一般风险，3-低风险
     */
    //private String riskLevel;
    private String mobileMe;
    private String isDefend;
    private String checkStatus;
    /**
     * 删除状态:正常：0；已删除：1
     */
    private String deleted;
    /**
     * 创建时间
     */
    private LocalDateTime createDate;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 更新时间（新创建的数据更新时间和创建时间相同）
     */
    private LocalDateTime updateDate;
    /**
     * 更新人
     */
    private String updateBy;
    private String updateByMobile;
}
