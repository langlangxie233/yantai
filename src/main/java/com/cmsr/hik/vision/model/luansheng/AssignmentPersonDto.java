package com.cmsr.hik.vision.model.luansheng;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/7/2 10:55
 */
@Setter
@Getter
public class AssignmentPersonDto {

    private String assignPersonnelId;
    private String assignPersonnelPhone;
}
