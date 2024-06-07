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
public class SecEmployeeRealLocationRecord {
    private String dataId;
    private String systemName;
    private String areaCode;
    private List<SecEmployeeRealLocationRecordData> data;

}
