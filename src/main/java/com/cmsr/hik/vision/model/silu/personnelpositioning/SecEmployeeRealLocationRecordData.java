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
public class SecEmployeeRealLocationRecordData {
    private String card_no;
    private String company_social_code;
    private String floor_no;
    private String latitude;
    private String longitude;
    private String status;
    private String time_stamp;
    private String update_time;

}
