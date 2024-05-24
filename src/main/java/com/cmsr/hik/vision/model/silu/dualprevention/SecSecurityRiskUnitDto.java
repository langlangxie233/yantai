package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 安全风险分析单元
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSecurityRiskUnitDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 风险分析对象编码即危险化学品登记信息管理系统中的危险源编码
     */
    private String hazardCode;
    /**
     * 风险分析对象所属部门名称
     */
    private String hazardDep;
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
