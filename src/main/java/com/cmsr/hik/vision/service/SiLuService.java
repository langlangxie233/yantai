package com.cmsr.hik.vision.service;

import com.alibaba.fastjson2.JSON;
import com.cmsr.hik.vision.config.silu.SiLuHttpConfig;
import com.cmsr.hik.vision.model.silu.dualprevention.*;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeAlarmDataDto;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeFileDto;
import com.cmsr.hik.vision.model.silu.personnelpositioning.SecEmployeeRealLocationDto;
import com.cmsr.hik.vision.model.silu.result.ResponseData;
import com.cmsr.hik.vision.model.silu.result.ResponseObject;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecDeviceRunStatusDto;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecProductionPromiseDto;
import com.cmsr.hik.vision.model.silu.securitycommitment.SecProductionPromiseTicketDto;
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

import java.util.*;

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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT `id`, `companyCode`, `hazardCode`, `hazardDep`, `hazardLiablePerson`, `riskUnitName`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_unit AS unit ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_unit order by createDate desc limit 1";
        String path = "/sec_security_risk_unit";
        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "WHERE updateDate > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            ////log.info("<=====================sql语句==============================>");
            ////log.info(sql);
            try{
                List<SecSecurityRiskUnitDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskUnitDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        ////log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT events.`id`, events.`companyCode`, events.`riskUnitId`, events.`riskEventName`, events.`deleted`, events.`createDate`, events.`createBy`, events.`updateDate`, events.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_events AS events \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_events order by createDate desc limit 1";
        String path = "/sec_security_risk_events";

        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "WHERE `events`.`updateDate` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `events`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            ////log.info("<=====================sql语句==============================>");
            ////log.info(sql);
            try{
                List<SecSecurityRiskEventsDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskEventsDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT control_measure.`id`,control_measure.`companyCode`,control_measure.`riskEventId`,control_measure.`dataSrc`,control_measure.`riskMeasureDesc`,control_measure.`classify1`,control_measure.`classify2`,control_measure.`classify3`,control_measure.`troubleshootContent`,control_measure.`deleted`,control_measure.`createDate`,control_measure.`createBy`,control_measure.`updateDate`,control_measure.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_control_measures AS control_measure \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_control_measures order by createDate desc limit 1";
        String path = "/sec_security_risk_control_measures";

        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `control_measure`.`updateDate` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `control_measure`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecSecurityRiskControlMeasuresDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskControlMeasuresDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_mission.`id`,check_mission.`companyCode`,check_mission.`riskMeasureId`,check_mission.`troubleshootContent`,check_mission.`checkCycle`,check_mission.`checkCycleUnit`,check_mission.`taskStartTime`,check_mission.`workStartTime`,check_mission.`workEndTime`,check_mission.`workDayType`,check_mission.`workType`,check_mission.`taskNum`,check_mission.`deleted`,check_mission.`createDate`,check_mission.`createBy`,check_mission.`updateDate`,check_mission.`updateBy` \n" +
                "from ythg_ods.dwd_sec_hidden_check_mission AS check_mission \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_mission order by createDate desc limit 1";
        String path = "/sec_hidden_check_mission";

        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `check_mission`.`updateDate` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `check_mission`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenCheckMissionDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckMissionDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_record.`id`, check_record.`companyCode`, check_record.`checkTaskId`, check_record.`checkTime`, check_record.`mobileMe`, check_record.`isDefend`, check_record.`checkStatus`, check_record.`deleted`, check_record.`createDate`, check_record.`createBy`, check_record.`createByMobile`, check_record.`updateDate`, check_record.`updateBy`, check_record.`updateByMobile` \n" +
                "FROM ythg_ods.dwd_sec_hidden_check_record AS check_record \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_record order by createDate desc limit 1";
        String path = "/sec_hidden_check_record";

        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `check_record`.`checkTime` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `check_record`.`checkTime` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenCheckRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckRecordDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT danger_info.`id`, danger_info.`companyCode`, danger_info.`hazardCode`, danger_info.`riskMeasureId`, danger_info.`checkRecordId`, danger_info.`dangerName`, danger_info.`dangerLevel`, danger_info.`registTime`, danger_info.`registrant`, danger_info.`dangersSrc`, danger_info.`enforcementId`, danger_info.`dangerManageType`, danger_info.`hazardDangerType`, danger_info.`hazardCategory`, danger_info.`dangerDesc`, danger_info.`dangerReason`, danger_info.`controlMeasures`, danger_info.`cost`, danger_info.`liablePerson`, danger_info.`dangerManageDeadline`, danger_info.`checkAcceptPerson`, danger_info.`checkAcceptTime`, danger_info.`checkAcceptComment`, danger_info.`dangerState`, danger_info.`dangerImg`, danger_info.`dangerAcceptImg`, danger_info.`deleted`, danger_info.`createDate`, danger_info.`createBy`, danger_info.`updateDate`, danger_info.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_hidden_danger_info AS danger_info \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_danger_info order by createDate desc limit 1";
        String path = "/sec_hidden_danger_info";

        int pageNo = 0;
        int pageSize = 50;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `danger_info`.`updateDate` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `danger_info`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenDangerInfoDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenDangerInfoDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `maintenance_record`.`id`, `maintenance_record`.`companyCode`, `maintenance_record`.`hazardCode`, `maintenance_record`.`riskUnitId`, `maintenance_record`.`stopStartTime`, `maintenance_record`.`stopEndTime`, `maintenance_record`.`stopReason`, `maintenance_record`.`deleted`, `maintenance_record`.`createDate`, `maintenance_record`.`createBy`, `maintenance_record`.`updateDate`, `maintenance_record`.`updateBy` " +
                "from ythg_ods.dwd_sec_deactivated_maintenance_record maintenance_record ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_deactivated_maintenance_record";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where `maintenance_record`.`updateDate` > '" + DateTimeUtil.getHoursBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by `maintenance_record`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecDeactivatedMaintenanceRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecDeactivatedMaintenanceRecordDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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
        params.clear();
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
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecProductionPromiseDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecProductionPromiseDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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
        params.clear();
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
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecDeviceRunStatusDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecDeviceRunStatusDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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
        params.clear();
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
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecProductionPromiseTicketDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecProductionPromiseTicketDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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
    //endregion
    /**
     * 人员定位
     */
    //region
    public String updateSecEmployeeFile(String firstFlag) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select employee_file.`id`, employee_file.`employeeName`, employee_file.`sex`, employee_file.`imei`, employee_file.`phone`, employee_file.`post`, employee_file.`orgCode`, company_info.`enterprise_name` as enterpriseName \n" +
                "from ythg_ods.dwd_sec_employee_file as employee_file \n" +
                "inner join ythg_ods.dim_company_industry_type as company_info \n" +
                "on employee_file.`orgCode` = company_info.`org_code`\n" +
                "order by `id` desc ";
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
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select location.`lon`, location.`lat`, location.`imei`, location.`time` \n" +
                "from ythg_ods.dwd_sec_employee_real_loaction as location \n" +
                "INNER JOIN ythg_ods.dwd_sec_employee_file AS info on location.imei = info.imei \n";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_employee_real_loaction";

        int pageNo = 0;
        int pageSize = 20;
        if (!"1".equals(firstFlag)) {
            querySql = querySql + "where location.`time` > '" + DateTimeUtil.getMinutesBefore(2) + "' ";
        } else {
            log.warn("首次更新数据库中······");
        }
        querySql = querySql + "order by location.`time` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecEmployeeRealLocationDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecEmployeeRealLocationDto.class));
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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
        params.clear();
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
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
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
                        //log.info(response);
                        /*if ("500".equals(responseObject.getData().getCode())) {
                            //log.info(response);
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

    /*public String updateSecEmployeeRealLocationInstance(String msg) {
        log.info("人员实时定位kafkaMsg:" + msg);
        return null;
    }

    public String updateSecEmployeeAlarmDataInstance(String msg) {
        log.info("人员告警kafkaMsg:" + msg);
        return null;
    }*/
    //endregion

    /**
     * 上报安全风险分析单元数据-test
     *
     * @return 更新结果
     */
    //region
    public String updateSecurityRiskUnitTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT `id`, `companyCode`, `hazardCode`, `hazardDep`, `hazardLiablePerson`, `riskUnitName`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_unit AS unit ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_unit order by createDate desc limit 1";
        String path = "/sec_security_risk_unit";
        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE companyCode = '" + companyCode + "' order by `updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            log.info("<=====================sql语句==============================>");
            log.info(sql);
            try{
                List<SecSecurityRiskUnitDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskUnitDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateSecurityRiskEventsTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT events.`id`, events.`companyCode`, events.`riskUnitId`, events.`riskEventName`, events.`deleted`, events.`createDate`, events.`createBy`, events.`updateDate`, events.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_events AS events ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_events order by createDate desc limit 1";
        String path = "/sec_security_risk_events";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE `events`.companyCode = '" + companyCode + "' order by `events`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecSecurityRiskEventsDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskEventsDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateSecurityRiskControlMeasuresTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT control_measure.`id`,control_measure.`companyCode`,control_measure.`riskEventId`,control_measure.`dataSrc`,control_measure.`riskMeasureDesc`,control_measure.`classify1`,control_measure.`classify2`,control_measure.`classify3`,control_measure.`troubleshootContent`,control_measure.`deleted`,control_measure.`createDate`,control_measure.`createBy`,control_measure.`updateDate`,control_measure.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_security_risk_control_measures AS control_measure ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_security_risk_control_measures order by createDate desc limit 1";
        String path = "/sec_security_risk_control_measures";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE `control_measure`.companyCode = '" + companyCode + "' order by `control_measure`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecSecurityRiskControlMeasuresDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSecurityRiskControlMeasuresDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateHiddenCheckMissionTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_mission.`id`,check_mission.`companyCode`,check_mission.`riskMeasureId`,check_mission.`troubleshootContent`,check_mission.`checkCycle`,check_mission.`checkCycleUnit`,check_mission.`taskStartTime`,check_mission.`workStartTime`,check_mission.`workEndTime`,check_mission.`workDayType`,check_mission.`workType`,check_mission.`taskNum`,check_mission.`deleted`,check_mission.`createDate`,check_mission.`createBy`,check_mission.`updateDate`,check_mission.`updateBy` \n" +
                "from ythg_ods.dwd_sec_hidden_check_mission AS check_mission ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_mission order by createDate desc limit 1";
        String path = "/sec_hidden_check_mission";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE `check_mission`.companyCode = '" + companyCode + "' order by `check_mission`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenCheckMissionDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckMissionDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateHiddenCheckRecordTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT check_record.`id`, check_record.`companyCode`, check_record.`checkTaskId`, check_record.`checkTime`, check_record.`mobileMe`, check_record.`isDefend`, check_record.`checkStatus`, check_record.`deleted`, check_record.`createDate`, check_record.`createBy`, check_record.`createByMobile`, check_record.`updateDate`, check_record.`updateBy`, check_record.`updateByMobile` \n" +
                "FROM ythg_ods.dwd_sec_hidden_check_record AS check_record ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_check_record order by createDate desc limit 1";
        String path = "/sec_hidden_check_record";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "where `check_record`.companyCode = '" + companyCode + "' order by `check_record`.`checkTime` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenCheckRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenCheckRecordDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateHiddenDangerInfoTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "SELECT danger_info.`id`, danger_info.`companyCode`, danger_info.`hazardCode`, danger_info.`riskMeasureId`, danger_info.`checkRecordId`, danger_info.`dangerName`, danger_info.`dangerLevel`, danger_info.`registTime`, danger_info.`registrant`, danger_info.`dangersSrc`, danger_info.`enforcementId`, danger_info.`dangerManageType`, danger_info.`hazardDangerType`, danger_info.`hazardCategory`, danger_info.`dangerDesc`, danger_info.`dangerReason`, danger_info.`controlMeasures`, danger_info.`cost`, danger_info.`liablePerson`, danger_info.`dangerManageDeadline`, danger_info.`checkAcceptPerson`, danger_info.`checkAcceptTime`, danger_info.`checkAcceptComment`, danger_info.`dangerState`, danger_info.`dangerImg`, danger_info.`dangerAcceptImg`, danger_info.`deleted`, danger_info.`createDate`, danger_info.`createBy`, danger_info.`updateDate`, danger_info.`updateBy` \n" +
                "FROM ythg_ods.dwd_sec_hidden_danger_info AS danger_info ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_hidden_danger_info order by createDate desc limit 1";
        String path = "/sec_hidden_danger_info";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE `danger_info`.companyCode = '" + companyCode + "' order by `danger_info`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecHiddenDangerInfoDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecHiddenDangerInfoDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        log.info("response：" + response);
                        responseObject = JSON.to(ResponseObject.class, response);
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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

    public String updateDeactivatedMaintenanceRecordTest(String companyCode) {
        params.clear();
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `maintenance_record`.`id`, `maintenance_record`.`companyCode`, `maintenance_record`.`hazardCode`, `maintenance_record`.`riskUnitId`, `maintenance_record`.`stopStartTime`, `maintenance_record`.`stopEndTime`, `maintenance_record`.`stopReason`, `maintenance_record`.`deleted`, `maintenance_record`.`createDate`, `maintenance_record`.`createBy`, `maintenance_record`.`updateDate`, `maintenance_record`.`updateBy` " +
                "from ythg_ods.dwd_sec_deactivated_maintenance_record maintenance_record ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_deactivated_maintenance_record";

        int pageNo = 0;
        int pageSize = 20;
        querySql = querySql + "WHERE `maintenance_record`.companyCode = '" + companyCode + "' order by `maintenance_record`.`updateDate` desc limit ";
        while (whileFlag) {
            String sql = querySql + pageNo * pageSize + ", " + pageSize;
            //log.info("<=====================sql语句==============================>");
            //log.info(sql);
            try{
                List<SecDeactivatedMaintenanceRecordDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecDeactivatedMaintenanceRecordDto.class));
                if (!list.isEmpty()) {
                    //log.info("<====================数据库查到的数据===========================>");
                    //log.info("list.size:" + list.size());
                    //list.forEach(l -> log.info(l.getId()));
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
                        if ("500".equals(responseObject.getData().getCode())) {
                            log.info(response);
                            //list.forEach(l -> log.info(l.getRiskUnitId()));
                            //whileFlag = false;
                        }
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