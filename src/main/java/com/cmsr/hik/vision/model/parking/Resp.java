package com.cmsr.hik.vision.model.parking;

import lombok.Data;

import java.util.List;

@Data
public class Resp<T> {
    private String code;
    private String msg;
    private RespData<T> data;

    @Data
    public static class DataObject<P> {
        private Integer total;
        private Integer pageNo;
        private Integer pageSize;
        private List<P> list;
    }
}
