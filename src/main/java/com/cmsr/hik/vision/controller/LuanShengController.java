package com.cmsr.hik.vision.controller;

import com.cmsr.hik.vision.model.luansheng.*;
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
    @PostMapping(value = "/view/emergency/alarm/reception", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj adAlarmReception(@RequestBody AlarmReceptionDto dto) {
        return luanShengService.adAlarmReception(dto);
    }

    /**
     * 人工接警——>应急突发事件表
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/accident/plan/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj accidentUpdate(@RequestBody AccidentUpdateDto dto) {
        return luanShengService.accidentUpdate(dto);
    }

    /**
     * 应急演练——>应急演练记录表
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/emergency/drill", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addEmergencyDrill(@RequestBody EmergencyDrillDto dto) {
        return luanShengService.addEmergencyDrill(dto);
    }

    /**
     * 应急——>任务指派接口
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/task/assignment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addTaskAssignment(@RequestBody EmergencyTaskAssignmentDto dto) {
        return luanShengService.addTaskAssignment(dto);
    }

    /**
     * 应急——>应急终止页面接口
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/termination", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addTermination(@RequestBody EmergencyTerminationDto dto) {
        return luanShengService.addTermination(dto);
    }

    /**
     * 应急——>总结评估录入接口
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/summary/evaluation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addSummaryEvaluation(@RequestBody EmergencySummaryEvaluationDto dto) {
        return luanShengService.addSummaryEvaluation(dto);
    }

    /**
     * 应急——>演练计划上报接口
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/exercise/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj addExercisePlan(@RequestBody ExercisePlanDto dto) {
        return luanShengService.addExercisePlan(dto);
    }

    /**
     * 应急——>更新关注状态
     *
     * @return 更新结果
     */
    @PostMapping(value = "/view/emergency/plan/organization/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultObj updatePlanOrganization(@RequestBody EmergencyPlanOrganizationUpdateDto dto) {
        return luanShengService.updatePlanOrganization(dto);
    }
}
