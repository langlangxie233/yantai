package com.cmsr.hik.vision.model.anjian.result;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/5/7 15:59
 */
@Getter
@Setter
public class GetTokenResponse {

    private String status;
    private String desp;
    private GetTokenResponseObject data;
}
