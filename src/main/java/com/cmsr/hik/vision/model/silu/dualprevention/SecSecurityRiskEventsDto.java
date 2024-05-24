package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报安全风险事件数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSecurityRiskEventsDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 风险分析单元 ID
     */
    private String riskUnitId;
    /**
     * 风险事件名称
     */
    private String riskEventName;
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
