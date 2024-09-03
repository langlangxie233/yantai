package com.cmsr.hik.vision.model.luansheng;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/7/2 10:55
 */
@Getter
@Setter
public class AccidentUpdateDto {
    @NotNull
    private String id;
    @NotNull
    private String emergencyPlanId;
}
