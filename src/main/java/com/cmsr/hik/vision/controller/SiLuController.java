package com.cmsr.hik.vision.controller;

import com.cmsr.hik.vision.service.SiLuJobTicketService;
import com.cmsr.hik.vision.service.SiLuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据上报
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 15:05
 */
@RestController
public class SiLuController {
    @Autowired
    private SiLuService siLuService;
    @Autowired
    private SiLuJobTicketService siLuJobTicketService;
    //风险分析
    //region
    /**
     * 上报安全风险分析单元数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/security/risk/unit", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecurityRiskUnit(@RequestParam String firstFlag) {
        return siLuService.updateSecurityRiskUnit(firstFlag);
    }

    /**
     * 上报安全风险事件数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/security/risk/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecurityRiskEvents(@RequestParam String firstFlag) {
        return siLuService.updateSecurityRiskEvents(firstFlag);
    }

    /**
     * 上报安全风险管控措施数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/security/risk/control/measures", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecurityRiskControlMeasures(@RequestParam String firstFlag) {
        return siLuService.updateSecurityRiskControlMeasures(firstFlag);
    }

    /**
     * 上报隐患排查任务数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/hidden/check/mission", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateHiddenCheckMission(@RequestParam String firstFlag) {
        return siLuService.updateHiddenCheckMission(firstFlag);
    }

    /**
     * 上报隐患排查记录数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/hidden/check/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateHiddenCheckRecord(@RequestParam String firstFlag) {
        return siLuService.updateHiddenCheckRecord(firstFlag);
    }

    /**
     * 上报隐患信息数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/hidden/danger/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateHiddenDangerInfo(@RequestParam String firstFlag) {
        return siLuService.updateHiddenDangerInfo(firstFlag);
    }

    /**
     * 上报停用/检修记录
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/deactivated/maintenance/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateDeactivatedMaintenanceRecord(@RequestParam String firstFlag) {
        return siLuService.updateDeactivatedMaintenanceRecord(firstFlag);
    }
    //endregion
    //安全
    //region
    /**
     * 上报安全承诺数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/production/promise", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecProductionPromise(@RequestParam String firstFlag) {
        return siLuService.updateSecProductionPromise(firstFlag);
    }

    /**
     * 上报承诺装置运行状态表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/device/run/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecDeviceRunStatus(@RequestParam String firstFlag) {
        return siLuService.updateSecDeviceRunStatus(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/production/promise/ticket", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecProductionPromiseTicket(@RequestParam String firstFlag) {
        return siLuService.updateSecProductionPromiseTicket(firstFlag);
    }
    //endregion
    //特殊作业票
    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/approval", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketApproval(@RequestParam String firstFlag) {
        return siLuService.updateSecSpecialJobTicketApproval(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/blindplate", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketBlindplate(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketBlindplate(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/break", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketBreak(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketBreak(firstFlag);
    }


    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/fire", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketFire(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketFire(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/gas/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketGasAnalysis(@RequestParam String firstFlag) {
        return siLuService.updateSecSpecialJobTicketGasAnalysis(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/high", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketHigh(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketHigh(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/hoising", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketHoising(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketHoising(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/maintenance", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketMaintenance(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketMaintenance(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/sec/special/job/ticket/pourback", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketPourback(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketPourback(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/pour", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketPour(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketPour(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/power", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketPower(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketPower(firstFlag);
    }

    /**
     * 特殊作业数据上报-上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/safety/measures", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketSafetyMeasures(@RequestParam String firstFlag) {
        return siLuService.updateSecSpecialJobTicketSafetyMeasures(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/soil", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketSoil(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketSoil(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/space", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketSpace(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketSpace(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/special/job/ticket/water", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialJobTicketWater(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialJobTicketWater(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/employee/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecEmployeeFile(@RequestParam String firstFlag) {
        return siLuService.updateSecEmployeeFile(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/employee/real/loaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecEmployeeRealLoaction(@RequestParam String firstFlag) {
        return siLuService.updateSecEmployeeRealLoaction(firstFlag);
    }

    /**
     * 上报承诺作业票详情表
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    @GetMapping(value = "/silu/sec/employee/alarm/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecEmployeeAlarmData(@RequestParam String firstFlag) {
        return siLuService.updateSecEmployeeAlarmData(firstFlag);
    }

    @GetMapping(value = "/silu/sec/speicial/ticket/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateSecSpecialTicketFile(@RequestParam String firstFlag) {
        return siLuJobTicketService.updateSecSpecialTicketFile(firstFlag);
    }

}
