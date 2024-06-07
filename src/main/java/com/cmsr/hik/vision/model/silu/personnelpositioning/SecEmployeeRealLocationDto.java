package com.cmsr.hik.vision.model.silu.personnelpositioning;

import lombok.Getter;
import lombok.Setter;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecEmployeeRealLocationDto {
    private String lon;
    private String lat;
    private String imei;
    private String time;

}
