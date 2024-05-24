package com.cmsr.hik.vision.model.silu.result;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ResponseData {
    String msg;
    String code;
    List<ResultData> data;
}
