package com.cmsr.hik.vision.controller;

import com.cmsr.hik.vision.model.luansheng.AlarmReceptionDto;
import com.cmsr.hik.vision.model.luansheng.EmergencyDrillDto;
import com.cmsr.hik.vision.service.LuanShengService;
import com.cmsr.hik.vision.vo.ResultObj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 孪生接口对接
 *
 * @author 上研院 xiexianlang
 * @date 2024/7/1 15:28
 */
@RestController
public class LuanShengController {
    @Autowired
    private LuanShengService luanShengService;

    /**
     * 人工接警——>应急突发事件表
     *
     * @return 更新结果
     */
    @PostMapping(value = "/luansheng/alarm/reception", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj adAlarmReception(@RequestBody AlarmReceptionDto dto) {
        return luanShengService.adAlarmReception(dto);
    }

    /**
     * 应急演练——>应急演练记录表
     *
     * @return 更新结果
     */
    @PostMapping(value = "/luansheng/emergency/drill", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addEmergencyDrill(@RequestBody EmergencyDrillDto dto) {
        return luanShengService.addEmergencyDrill(dto);
    }


}
