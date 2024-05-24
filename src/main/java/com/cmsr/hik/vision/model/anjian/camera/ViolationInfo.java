package com.cmsr.hik.vision.model.anjian.camera;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/5/14 13:58
 */
@Getter
@Setter
public class ViolationInfo {
    private String id;
    private String metadata;
    private String recordDuration;
    private String superUserId;
    private String message;
    private String userName;
    private String handleUserId;
    private String recordFps;
    private String picPath;
    private String cameraId;
    private String violationName;
    private String addressName;
    private String recognitionName;
    private String recordPath;
    private String cameraName;
    private Integer status;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}