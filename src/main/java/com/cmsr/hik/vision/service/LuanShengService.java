package com.cmsr.hik.vision.service;

import com.cmsr.hik.vision.model.luansheng.AlarmReceptionDto;
import com.cmsr.hik.vision.model.luansheng.EmergencyDrillDto;
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


    public ResultObj adAlarmReception(AlarmReceptionDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            dto.setId(UUID.randomUUID().toString());
            String sql="insert into ythg_ods.ods_silu_emergency_emg_emergency (`id`,`accident_name`,`alarm_time`,`accident_type`,`latitude`,`longitude`,`enterprise_name`,`reported_name`,`responder`,`accident_description`,`judgment_process_record`,`deleted`) VALUES " +
                    "(" +
                    "'" + dto.getId() + "', "  +
                    "'" + dto.getAccidentName() + "', "  +
                    "'" + dto.getAlarmTime() + "', "  +
                    "'" + dto.getAccidentType() + "', "  +
                    "'" + dto.getLatitude() + "', "  +
                    "'" + dto.getLongitude() + "', "  +
                    "'" + dto.getEnterpriseName() + "', "  +
                    "'" + dto.getReportedName() + "', "  +
                    "'" + dto.getResponder() + "', "  +
                    "'" + dto.getAccidentDescription() + "', "  +
                    "'" + dto.getJudgmentProcessRecord() + "', "  +
                    "'" + dto.getDelFlag() + "'" +
                    ")";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }

    public ResultObj addEmergencyDrill(EmergencyDrillDto dto) {
        ResultObj resultObj = new ResultObj();
        resultObj.setCode(200);
        resultObj.setMsg("数据录入成功");
        //入库
        try {
            dto.setId(UUID.randomUUID().toString());
            String sql="insert into emergency.emg_exercise_record (`id`,`plan_code`,`execrise_name`,`exercise_type`,`execrise_purpose`,`drill_end_time`,`number`,`address`,`execrise_process`,`execrise_summary`,`eval_reason`,`del_flag`) VALUES " +
                    "(" +
                    "'" + dto.getId() + "', "  +
                    "'" + dto.getPlanCode() + "', "  +
                    "'" + dto.getExerciseName() + "', "  +
                    "'" + dto.getExerciseType() + "', "  +
                    "'" + dto.getExercisePurpose() + "', "  +
                    "'" + dto.getDrillEndTime() + "', "  +
                    "'" + dto.getNumber() + "', "  +
                    "'" + dto.getAddress() + "', "  +
                    "'" + dto.getExercisePurpose() + "', "  +
                    "'" + dto.getExerciseSummary() + "', "  +
                    "'" + dto.getEvalReason() + "', "  +
                    "'" + dto.getDelFlag() + "', " +
                    ")";
            anJianDorisTemplate.update(sql);
        } catch (Exception e) {
            resultObj.setCode(400);
            resultObj.setMsg("数据录入失败");
            log.error("数据录入失败", e);
        }
        return resultObj;
    }
}
