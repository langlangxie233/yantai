package com.cmsr.hik.vision.model.silu.dualprevention;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报安全风险管控措施数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSecurityRiskControlMeasuresDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;
    /**
     * 风险事件 ID
     */
    private String riskEventId;
    /**
     * 管控方式自动化监控：1；
     * 隐患排查：2
     */
    private String dataSrc;
    /**
     * 管控措施描述
     */
    private String riskMeasureDesc;
    /**
     * 管控措施分类 1管控措施分类
     * （工程技术：1； 维护保养：2； 操作行为：3； 应急措施：4）
     */
    private String classify1;
    /**
     * 管控措施分类 2工艺控制:1-1；关键设备/部件：1-2；安全附件：1-3；安全仪表：1-4；其它：1-5；动设备：2-1；静设备：2-2；其它：2-3；人员资质：3-1；操作记录：3-2；交接班：3-3；其它：3-4；应急设施：4-1；个体防护：4-2；消防设施：4-3；应急预案：4-4。其它：4-5；填写编码
     */
    private String classify2;
    /**
     * 管控措施分类3由企业自行定义
     */
    private String classify3;
    /**
     * 隐 患 排 查 内 容
     */
    private String troubleshootContent;
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
