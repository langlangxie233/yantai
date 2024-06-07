package com.cmsr.hik.vision.model.silu.personnelpositioning;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecEmployeeAlarmDataRecordData {
    private String id;
    private String time;
    private String type;
    private String area_name;
    private String name;
    private String card_no;
    private String handle_time;
    private String handle_remark;
    private String longitude;
    private String latitude;
    private String company_social_code;
    private String update_time;
}
