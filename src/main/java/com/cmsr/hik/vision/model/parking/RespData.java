package com.cmsr.hik.vision.model.parking;

import lombok.Data;

import java.util.List;

@Data
public class RespData<T> {
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> list;

}
