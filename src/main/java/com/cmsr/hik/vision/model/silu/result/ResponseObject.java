package com.cmsr.hik.vision.model.silu.result;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseObject {
    String requestId;
    Boolean success;
    Integer code;
    Integer state;
    ResponseData data;
}

