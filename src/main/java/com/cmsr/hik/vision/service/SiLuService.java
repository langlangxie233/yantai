package com.cmsr.hik.vision.service;

import com.alibaba.fastjson2.JSON;
import com.cmsr.hik.vision.config.silu.SiLuHttpConfig;
import com.cmsr.hik.vision.enums.TicketFileTypeEnum;
import com.cmsr.hik.vision.model.silu.dualprevention.*;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeAlarmDataDto;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeFileDto;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeRealLoactionDto;
import com.cmsr.hik.vision.model.silu.result.ResponseData;
import com.cmsr.hik.vision.model.silu.result.ResponseObject;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecDeviceRunStatusDto;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecProductionPromiseDto;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecProductionPromiseTicketDto;
import com.cmsr.hik.vision.model.silu.specialassignments.*;
import com.cmsr.hik.vision.utils.AESUtils;
import com.cmsr.hik.vision.utils.DateTimeUtil;
import com.cmsr.hik.vision.utils.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据上报
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 15:05
 */
@Data
@Slf4j
@Service
public class SiLuService {

    @Autowired
    private SiLuHttpConfig httpConfig;

    @Autowired
    @Qualifier("siLuDorisTemplate")
    private JdbcTemplate siLuDorisTemplate;

    @Autowired
    private MinioService minioService;

    private Map<String, String> params = new HashMap<>();

    /**
     * 上报安全风险分析单元数据
     *
     * @param firstFlag 是否第一次更新数据：1-是，0-否
     * @return 更新结果
     */
    //region
    public String updateSecurityRiskUnit(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT `id`, `companyCode`, `hazardCode`, `hazardDep`, `hazardLiablePerson`, `riskUnitName`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_unit AS unit\n" +
                "WHERE deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_unit order by createDate desc limit 1";
        String path = "/sec_security_risk_unit";
        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSecurityRiskUnitDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskUnitDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);
                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecurityRiskEvents(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT events.`id`, events.`companyCode`, events.`riskUnitId`, events.`riskEventName`, events.`deleted`, events.`createDate`, events.`createBy`, events.`updateDate`, events.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_events AS events \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_unit AS unit on events.riskUnitId = unit.`id` \n" +
                "WHERE unit.deleted = '0' AND events.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_events order by createDate desc limit 1";
        String path = "/sec_security_risk_events";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `events`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `events`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSecurityRiskEventsDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskEventsDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecurityRiskControlMeasures(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT control_measure.`id`,control_measure.`companyCode`,control_measure.`riskEventId`,control_measure.`dataSrc`,control_measure.`riskMeasureDesc`,control_measure.`classify1`,control_measure.`classify2`,control_measure.`classify3`,control_measure.`troubleshootContent`,control_measure.`deleted`,control_measure.`createDate`,control_measure.`createBy`,control_measure.`updateDate`,control_measure.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_control_measures AS control_measure \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_events events on control_measure.riskEventId = events.`id`\n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_unit AS unit on events.riskUnitId = unit.`id` \n" +
                "where control_measure.deleted = '0' and events.deleted = '0' AND unit.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_control_measures order by createDate desc limit 1";
        String path = "/sec_security_risk_control_measures";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `control_measure`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `control_measure`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSecurityRiskControlMeasuresDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskControlMeasuresDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateHiddenCheckMission(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_mission.`id`,check_mission.`companyCode`,check_mission.`riskMeasureId`,check_mission.`troubleshootContent`,check_mission.`checkCycle`,check_mission.`checkCycleUnit`,check_mission.`taskStartTime`,check_mission.`workStartTime`,check_mission.`workEndTime`,check_mission.`workDayType`,check_mission.`workType`,check_mission.`taskNum`,check_mission.`deleted`,check_mission.`createDate`,check_mission.`createBy`,check_mission.`updateDate`,check_mission.`updateBy` \n" +
                "from ythg_ods.dwd_sec_hidden_check_mission AS check_mission\n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_control_measures AS control_measure on check_mission.riskMeasureId = control_measure.`id` \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_events events on control_measure.riskEventId = events.`id`\n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_unit AS unit on events.riskUnitId = unit.`id` \n" +
                "WHERE check_mission.deleted = '0' AND control_measure.deleted = '0' and events.deleted = '0' AND unit.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_mission order by createDate desc limit 1";
        String path = "/sec_hidden_check_mission";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `check_mission`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `check_mission`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecHiddenCheckMissionDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckMissionDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateHiddenCheckRecord(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_record.`id`, check_record.`companyCode`, check_record.`checkTaskId`, check_record.`checkTime`, check_record.`mobileMe`, check_record.`isDefend`, check_record.`checkStatus`, check_record.`deleted`, check_record.`createDate`, check_record.`createBy`, check_record.`createByMobile`, check_record.`updateDate`, check_record.`updateBy`, check_record.`updateByMobile` \n" +
                "FROM ythg_ods.dwd_sec_hidden_check_record AS check_record\n" +
                "INNER JOIN ythg_ods.dwd_sec_hidden_check_mission AS check_mission ON check_record.checkTaskId = check_mission.`id` \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_control_measures AS control_measure ON check_mission.riskMeasureId = control_measure.`id` \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_events events ON control_measure.riskEventId = events.`id`\n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_unit AS unit ON events.riskUnitId = unit.`id` \n" +
                "WHERE check_record.deleted = '0' AND check_mission.deleted = '0' AND control_measure.deleted = '0' AND events.deleted = '0' AND unit.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_record order by createDate desc limit 1";
        String path = "/sec_hidden_check_record";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `check_record`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `check_record`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecHiddenCheckRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckRecordDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateHiddenDangerInfo(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT danger_info.`id`, danger_info.`companyCode`, danger_info.`hazardCode`, danger_info.`riskMeasureId`, danger_info.`checkRecordId`, danger_info.`dangerName`, danger_info.`dangerLevel`, danger_info.`registTime`, danger_info.`registrant`, danger_info.`dangersSrc`, danger_info.`enforcementId`, danger_info.`dangerManageType`, danger_info.`hazardDangerType`, danger_info.`hazardCategory`, danger_info.`dangerDesc`, danger_info.`dangerReason`, danger_info.`controlMeasures`, danger_info.`cost`, danger_info.`liablePerson`, danger_info.`dangerManageDeadline`, danger_info.`checkAcceptPerson`, danger_info.`checkAcceptTime`, danger_info.`checkAcceptComment`, danger_info.`dangerState`, danger_info.`dangerImg`, danger_info.`dangerAcceptImg`, danger_info.`deleted`, danger_info.`createDate`, danger_info.`createBy`, danger_info.`updateDate`, danger_info.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_hidden_danger_info AS danger_info\n" +
                "INNER JOIN ythg_ods.dwd_sec_hidden_check_record AS check_record ON danger_info.checkRecordId = check_record.`id`\n" +
                "INNER JOIN ythg_ods.dwd_sec_hidden_check_mission AS check_mission ON check_record.checkTaskId = check_mission.`id` \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_control_measures AS control_measure ON check_mission.riskMeasureId = control_measure.`id` \n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_events events ON control_measure.riskEventId = events.`id`\n" +
                "INNER JOIN ythg_ods.dwd_sec_security_risk_unit AS unit ON events.riskUnitId = unit.`id`  \n" +
                "where danger_info.deleted = '0' and check_record.deleted = '0' AND check_mission.deleted = '0' AND control_measure.deleted = '0' AND events.deleted = '0' AND unit.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_danger_info order by createDate desc limit 1";
        String path = "/sec_hidden_danger_info";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `danger_info`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `danger_info`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecHiddenDangerInfoDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenDangerInfoDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);
                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateDeactivatedMaintenanceRecord(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `maintenance_record`.`id`, `maintenance_record`.`companyCode`, `maintenance_record`.`hazardCode`, `maintenance_record`.`riskUnitId`, `maintenance_record`.`stopStartTime`, `maintenance_record`.`stopEndTime`, `maintenance_record`.`stopReason`, `maintenance_record`.`deleted`, `maintenance_record`.`createDate`, `maintenance_record`.`createBy`, `maintenance_record`.`updateDate`, `maintenance_record`.`updateBy` from ythg_ods.dwd_sec_deactivated_maintenance_record maintenance_record inner join ythg_ods.dwd_sec_security_risk_unit unit on maintenance_record.riskUnitId = unit.`id` where maintenance_record.deleted = '0' and unit.deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_deactivated_maintenance_record";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `maintenance_record`.`createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `maintenance_record`.`createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecDeactivatedMaintenanceRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecDeactivatedMaintenanceRecordDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    private String toJsonBody() {
        return JSON.toJSONString(params);
    }
    //endregion
    /**
     * 安全承诺
     */
    //region
    public String updateSecProductionPromise(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `unitsNumber`, `runNumber`, `parkNumber`, `firesNumber`, `fire1Number`, `fire2Number`, `roadworkNumber`, `soilworkNumber`, `highworkNumber`, `electricityworkNumber`, `liftingworkNumber`, `blindplateNumber`, `spaceworkNumber`, `inspectionNumber`, `pourOutNumber`, `cleanTankNumber`, `drainingNumber`, `contractorNumber`, `changedTaskNumber`, `contractor`, `trialProduction`, `openParking`, `openParkingNumber`, `workNumber`, `notWorkNumber`, `test`, `testNumber`, `overhaulWorkNumber`, `dangerProcessNumber`, `mHazards`, `riskGrade`, `commitDate`, `commitment`, `commitContent`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_production_promise where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_production_promise";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecProductionPromiseDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecProductionPromiseDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecDeviceRunStatus(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `promiseId`, `hazardCode`, `isTesting`, `runStatus`, `runStatusBeginTime`, `runStatusEndTime`, `runStatusReason`, `isChanged`, `changedTime`, `changedReason`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_device_run_status where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_device_run_status";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecDeviceRunStatusDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecDeviceRunStatusDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecProductionPromiseTicket(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `promiseId`, `ticketId`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_production_promise_ticket where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_production_promise_ticket";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecProductionPromiseTicketDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecProductionPromiseTicketDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    list.forEach(l -> log.info(l.getId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }
    //endregion
    /**
     * 特殊作业
     */
    //region
    public String updateSecSpecialJobTicketApproval(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `ticketId`, `processNodes`, `processPersonnel`, `processOpinion`, `processTime`, `signalImage`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_special_job_ticket_approval where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_approval";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSpecialJobTicketApprovalDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketApprovalDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecSpecialJobTicketSafetyMeasures(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `ticketId`, `serialNumber`, `measuresContent`, `isInvolve`, `confirmPerson`, `signalImage`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_special_job_ticket_safety_measures where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_safety_measures";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSpecialJobTicketSafetyMeasuresDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketSafetyMeasuresDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecSpecialJobTicketGasAnalysis(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `ticketID`, `ticketType`, `gasType`, `gasName`, `analysisResults`, `resultsUnit`, `analyst`, `analystTime`, `analystPart`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` from ythg_ods.dwd_sec_special_job_ticket_gas_analysis where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_gas_analysis";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `createDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSpecialJobTicketGasAnalysisDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketGasAnalysisDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }
    //endregion
    /**
     * 人员定位
     */
    //region
    public String updateSecEmployeeFile(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `employeeName`, `sex`, `imei`, `phone`, `post`, `orgCode`, `enterpriseName` from ythg_ods.dwd_sec_employee_file order by `id` desc ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_employee_file";

        int pageNo = 0;
        int pageSize = 20;
        /*if (!"1".equals(firstFlag)) {
            querySql = querySql + "and `createDate` > '" + DateTimeUtil.getYesterdayBeginTime() + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }*/
        querySql = querySql + "limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecEmployeeFileDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecEmployeeFileDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecEmployeeRealLoaction(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `lon`, `lat`, `imei`, `time` from ythg_ods.dwd_sec_employee_real_loaction ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_employee_real_loaction";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `time` > '" + DateTimeUtil.getMinutesBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `time` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecEmployeeRealLoactionDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecEmployeeRealLoactionDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecEmployeeAlarmData(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `thirdId`, `alarmType`, `alarmTime`, `locationCode`, `longitude`, `latitude`, `address`, `alarmReason`, `alarmSource`, `alarmStatus`, `image` from ythg_ods.dwd_sec_employee_alarm_data ";
        //String queryLatestDateTimeSql = "select alarmTime from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_employee_alarm_data";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `alarmTime` > '" + DateTimeUtil.getMinutesBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `alarmTime` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecEmployeeAlarmDataDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecEmployeeAlarmDataDto.class));
                if (!list.isEmpty()) {
                    log.info("<====================数据库查到的数据===========================>");
                    log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getRiskUnitId()));
                    //log.info("list:" + JSON.toJSONString(list));
                    //组装请求
                    params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                    //调用API发送数据
                    try {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                        String jsonBody = toJsonBody();
                        String url = httpConfig.getUrl() + path;
                        log.info("url：" + url);
                        //log.info("header：" + headers.toString());
                        //log.info("jsonBody：" + jsonBody);

                        String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                        responseObject = JSON.to(ResponseObject.class, response);
                        log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            whileFlag = false;
                        }*/
                    } catch (Exception e) {
                        log.info("请求错误：" + e);
                        whileFlag = false;
                    }
                    if (list.size() < pageSize) {
                        whileFlag = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                whileFlag = false;
            }
        }
        return responseObject.getData().getMsg();
    }
    //endregion
}