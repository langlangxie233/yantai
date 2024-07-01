package com.cmsr.hik.vision.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用请求结果
 *
 * @author 上研院 xiexianlang
 * @date 2024/6/26 14:59
 */
@Setter
@Getter
public class ResultObj {
    private Integer code;
    private String msg;
    private Object data;
}
