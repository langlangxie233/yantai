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
public class CameraInfo {
    private String id;
    private String picturePath;
    private String deviceIp;
    private String isCanControl;
    private String groupsId;
    private String devicePwd;
    private String deviceChannelList;
    private String englishName;
    private String groupsName;
    private String urlDirect;
    private String recognitions;
    private Integer type;
    private String roi;
    private String deviceId;
    private String deviceUser;
    private String flv;
    private String hls;
    private String addressId;
    private String catalogId;
    private String thirdDeviceId;
    private String thirdChannelId;
    private String rtspPort;
    private String name;
    private LocalDateTime updatedTime;
    private LocalDateTime createdTime;
    private String addressName;
    private String tenantId;
    private String devicePort;
    private Integer fence;
    private Integer status;
}