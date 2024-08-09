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
public class ExercisePlanDto {

    /**
     *     id, --应急演练计划ID
     */
    private String id;
    /**
     *     name, --演练计划名称
     */
    private String name;
    /**
     *     plan_type, --应演练类型
     */
    private String planType;
    /**
     *     scenarios_code, --预案编码
     */
    private String scenariosCode;
    /**
     *     exercise_level, --演练级别
     */
    private String exerciseLevel;
    /**
     *     exercise_scenario, --演练场景
     */
    private String exerciseScenario;
    /**
     *     completion_date, --预计完成日期
     */
    private String completionDate;
    /**
     *     number, --参与人数
     */
    private String number;
    /**
     *     plan_state, --计划状态
     */
    private String planState;
    /**
     *     relatedCase, --关联案例
     */
    private String relatedCase;
    /**
     *     address --演练地点
     */
    private String address;

}

