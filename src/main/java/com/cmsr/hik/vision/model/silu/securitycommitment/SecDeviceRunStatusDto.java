package com.cmsr.hik.vision.model.silu.securitycommitment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报承诺装置运行状态表
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecDeviceRunStatusDto {
    /**
     * 主键 UUID
     */
    private String id;
    private String promiseId;
    private String hazardCode;
    private String isTesting;
    private String runStatus;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime runStatusBeginTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime runStatusEndTime;
    private String runStatusReason;
    private String isChanged;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime changedTime;
    private String changedReason;

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
