package com.cmsr.hik.vision.service;

import com.cmsr.hik.vision.model.luansheng.*;
import com.cmsr.hik.vision.vo.ResultObj;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;


/**
 * 孪生对接
 *
 * @author 上研院 xiexianlang
 * @date 2024/7/1 15:29
 */
@Data
@Slf4j
@Service
public class LuanShengService {

    @Autowired
    @Qualifier("anJianDorisTemplate")
    private JdbcTemplate anJianDorisTemplate;

    /**
     * 人工接警——>应急突发事件表
     *
     * @return 更新结果
     */
    public ResultObj adAlarmReception(AlarmReceptionDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            dto.setId(UUID.randomUUID().toString());
            String sql="insert into ythg_ods.ods_silu_emergency_emg_emergency (`id`,`accident_name`,`alarm_time`,`accident_type`,`latitude`,`longitude`,`enterprise_name`,`reported_name`,`responder`,`accident_description`,`judgment_process_record`,`deleted`) VALUES ('" +
                    dto.getId() + "', '" +
                    dto.getAccidentName() + "', '" +
                    dto.getAlarmTime() + "', '" +
                    dto.getAccidentType() + "', '" +
                    dto.getLatitude() + "', '" +
                    dto.getLongitude() + "', '" +
                    dto.getEnterpriseName() + "', '" +
                    dto.getReportedName() + "', '" +
                    dto.getResponder() + "', '" +
                    dto.getAccidentDescription() + "', '" +
                    dto.getJudgmentProcessRecord() + "', '" +
                    dto.getDelFlag() + "')";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急演练——>应急演练记录表
     *
     * @return 更新结果
     */
    public ResultObj addEmergencyDrill(EmergencyDrillDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            dto.setId(UUID.randomUUID().toString());
            String sql="insert into emergency.emg_exercise_record (`id`,`plan_code`,`execrise_name`,`exercise_type`,`execrise_purpose`,`drill_end_time`,`number`,`address`,`execrise_process`,`execrise_summary`,`eval_reason`,`del_flag`) VALUES ('" +
                    dto.getId() + "', '" +
                    dto.getPlanCode() + "', '" +
                    dto.getExerciseName() + "', '" +
                    dto.getExerciseType() + "', '" +
                    dto.getExercisePurpose() + "', '" +
                    dto.getDrillEndTime() + "', '" +
                    dto.getNumber() + "', '" +
                    dto.getAddress() + "', '" +
                    dto.getExercisePurpose() + "', '" +
                    dto.getExerciseSummary() + "', '" +
                    dto.getEvalReason() + "', '" +
                    dto.getDelFlag() + "')";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急——>任务指派接口
     *
     * @return 更新结果
     */
    public ResultObj addTaskAssignment(EmergencyTaskAssignmentDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            if (!dto.getPersonDto().isEmpty()) {
                dto.getPersonDto().forEach(p -> {
                    String sql="insert into ythg_ods.ods_luansheng_task_assign (`id`,`plan_id`,`task_definition`," +
                            "`assign_personnel_id`,`assign_personnel_phone`,`assign_time`,`del_flag`) VALUES ('" +
                            UUID.randomUUID().toString() + "', '" +
                            dto.getPlanId() + "', '" +
                            dto.getTaskDefinition() + "', '" +
                            p.getAssignPersonnelId() + "', '" +
                            p.getAssignPersonnelPhone() + "', '" +
                            dto.getAssignTime() + "', '" +
                            dto.getDelFlag() + "')";
                    anJianDorisTemplate.update(sql);
                });
            }
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急——>应急终止页面接口
     *
     * @return 更新结果
     */
    public ResultObj addTermination(EmergencyTerminationDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            String sql="insert into ythg_ods.ods_luansheng_emergencies_stop (`id`,`plan_id`,`end_time`,`describe`,`del_flag`) VALUES ('" +
                    UUID.randomUUID().toString() + "', '" +
                    dto.getPlanId() + "', '" +
                    dto.getEndTime() + "', '" +
                    dto.getDescribe() + "', '" +
                    dto.getDelFlag() + "')";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急——>总结评估录入接口
     *
     * @return 更新结果
     */
    public ResultObj addSummaryEvaluation(EmergencySummaryEvaluationDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            String sql="insert into ythg_ods.ods_luansheng_summary_assess (`id`,`plan_id`,`accident_id`,`company_id`,`company_name`," +
                    "`incident_time`,`location`,`incident_type`,`accident_type`,`modified_by`,`initial_disposal_situation`," +
                    "`emergency_status`,`organizational_command_situation`,`implementation_status`,`work_situation`," +
                    "`dissemination_situation`,`guarantee_situation`,`summary`,`alarm_person`,`del_flag`) VALUES ('" +
                    UUID.randomUUID().toString() + "', '" +
                    dto.getPlanId() + "', '" +
                    dto.getAccidentId() + "', '" +
                    dto.getCompanyId() + "', '" +
                    dto.getCompanyName() + "', '" +
                    dto.getIncidentTime() + "', '" +
                    dto.getLocation() + "', '" +
                    dto.getIncidentType() + "', '" +
                    dto.getAccidentType() + "', '" +
                    dto.getModifiedBy() + "', '" +
                    dto.getInitialDisposalSituation() + "', '" +
                    dto.getEmergencyStatus() + "', '" +
                    dto.getOrganizationalCommandSituation() + "', '" +
                    dto.getImplementationStatus() + "', '" +
                    dto.getWorkSituation() + "', '" +
                    dto.getDisseminationSituation() + "', '" +
                    dto.getGuaranteeSituation() + "', '" +
                    dto.getSummary() + "', '" +
                    dto.getAlarmPerson() + "', '" +
                    dto.getDelFlag() + "')";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急——>演练计划上报接口
     *
     * @return 更新结果
     */
    public ResultObj addExercisePlan(ExercisePlanDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            String sql="insert into ythg_ods.ods_silu_emergency_emg_exercise_plan (`id`,`name`,`plan_type`,`scenarios_code`," +
                    "`exercise_level`,`exercise_scenario`,`completion_date`,`number`,`plan_state`,`related_case`,`address`) VALUES ('" +
                    UUID.randomUUID().toString() + "', '" +
                    dto.getName() + "', '" +
                    dto.getPlanType() + "', '" +
                    dto.getScenariosCode() + "', '" +
                    dto.getExerciseLevel() + "', '" +
                    dto.getExerciseScenario() + "', '" +
                    dto.getCompletionDate() + "', '" +
                    dto.getNumber() + "', '" +
                    dto.getPlanState() + "', '" +
                    dto.getRelatedCase() + "', '" +
                    dto.getAddress() + "')";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    /**
     * 应急——>更新关注状态
     *
     * @return 更新结果
     */
    public ResultObj updatePlanOrganization(EmergencyPlanOrganizationUpdateDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            String sql="update ythg_ods.ods_luansheng_attention_span set `noticed` = '" + dto.getNoticed() +
                    "' where `id` = '" + dto.getPlanId() +
                    "' and `member_id` = '" + dto.getMemberId() + "'";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }
}
