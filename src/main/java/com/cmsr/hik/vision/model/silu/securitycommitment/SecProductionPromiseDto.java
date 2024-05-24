package com.cmsr.hik.vision.model.silu.securitycommitment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报安全承诺数据
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecProductionPromiseDto {
    /**
     * 主键 UUID
     */
    private String id;
    /**
     * 危化品登记平台登记的9位企业编码
     */
    private String companyCode;

    private String unitsNumber;

    private Integer runNumber;

    private Integer parkNumber;

    private Integer firesNumber;

    private Integer fire1Number;

    private Integer fire2Number;

    private Integer roadworkNumber;

    private Integer soilworkNumber;

    private Integer highworkNumber;

    private Integer electricityworkNumber;

    private Integer liftingworkNumber;

    private Integer blindplateNumber;

    private Integer spaceworkNumber;

    private Integer inspectionNumber;

    private Integer pourOutNumber;

    private Integer cleanTankNumber;

    private Integer drainingNumber;

    private Integer contractorNumber;

    private Integer changedTaskNumber;

    private String contractor;

    private String trialProduction;

    private String openParking;

    private Integer openParkingNumber;

    private Integer workNumber;

    private Integer notWorkNumber;

    private String test;

    private Integer testNumber;

    private Integer overhaulWorkNumber;

    private Integer dangerProcessNumber;

    private String mHazards;

    private String riskGrade;

    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime commitDate;

    private String commitment;

    private String commitContent;

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
