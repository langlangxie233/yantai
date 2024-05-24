package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报停用/检修记录
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecDeactivatedMaintenanceRecordDto {
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
     * 所属风险单元ID
     */
    private String riskUnitId;
    /**
     * 停用/检修开始时间
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime stopStartTime;
    /**
     * 停用/检修结束时间
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime stopEndTime;
    /**
     * 停用/检修原因
     */
    private String stopReason;
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
