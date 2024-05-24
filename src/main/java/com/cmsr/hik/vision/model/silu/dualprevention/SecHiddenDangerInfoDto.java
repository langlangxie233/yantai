package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报隐患信息数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecHiddenDangerInfoDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 风险分析对象编码
     */
    private String hazardCode;
    /**
     * 管控措施主键 ID
     * 所有隐患排查任务产生的隐患必须绑定管控措施。不为空则会校验该ID是否存在
     */
    private String riskMeasureId;
    /**
     * 隐患排查记录
     * ID
     */
    private String checkRecordId;
    /**
     * 隐患名称
     */
    private String dangerName;
    /**
     * 隐患等级
     */
    private String dangerLevel;
    /**
     * 登记时间
     */
    private String registTime;
    /**
     * 登记人姓名
     */
    private String registrant;
    /**
     * 隐患来源
     */
    private String dangersSrc;
    /**
     * 执法编号
     */
    private String enforcementId;
    /**
     * 治理类型
     */
    private String dangerManageType;
    /**
     * 隐患类型
     */
    private String hazardDangerType;
    /**
     * 隐患类别
     */
    private String hazardCategory;
    /**
     * 隐患描述
     */
    private String dangerDesc;
    /**
     * 原因分析
     */
    private String dangerReason;
    /**
     * 控制措施
     */
    private String controlMeasures;
    /**
     * 资金
     */
    private String cost;
    /**
     * 整改责任人
     */
    private String liablePerson;
    /**
     * 隐患治理期限
     */
    private String dangerManageDeadline;
    /**
     * 验收人姓名
     */
    private String checkAcceptPerson;
    /**
     * 验收时间
     */
    private String checkAcceptTime;
    /**
     * 验收情况
     */
    private String checkAcceptComment;
    /**
     * 隐患状态
     */
    private String dangerState;
    /**
     * 隐患照片
     */
    private String dangerImg;
    /**
     * 整改后照片
     */
    private String dangerAcceptImg;
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
