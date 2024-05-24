package com.cmsr.hik.vision.model.silu.personnelpositioning;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecEmployeeFileDto {
    private String id;
    private String employeeName;
    private String sex;
    private String imei;
    private String phone;
    private String post;
    private String orgCode;
    private String enterpriseName;
}
