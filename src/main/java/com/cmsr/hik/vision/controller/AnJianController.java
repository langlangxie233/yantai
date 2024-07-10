package com.cmsr.hik.vision.controller;

import com.cmsr.hik.vision.service.AnJianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安监接口对接
 *
 * @author 上研院 xiexianlang
 * @date 2024/5/7 15:28
 */
@RestController
public class AnJianController {
    @Autowired
    private AnJianService anJianService;

    /**
     * 查询违规列表
     *
     * @return 更新结果
     */
    @GetMapping(value = "/video/violation", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateQueryViolationList() {
        return anJianService.updateQueryViolationList();
    }

    /**
     * 查询违规列表
     *
     * @return 更新结果
     */
    @GetMapping(value = "/video/violation/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateQueryViolationListAll() {
        return anJianService.updateQueryViolationListAll();
    }

    /**
     * 查询违规列表
     *
     * @return 更新结果
     */
    @GetMapping(value = "/video/violation/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateQueryViolationStatusList() {
        return anJianService.updateQueryViolationStatusList();
    }

    /**
     * 查询摄像机列表
     *
     * @return 更新结果
     */
    @GetMapping(value = "/video/camera/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateQueryCameraList() {
        return anJianService.updateQueryCameraList();
    }

}
