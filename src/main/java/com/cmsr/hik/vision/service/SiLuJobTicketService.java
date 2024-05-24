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
public class SiLuJobTicketService {

    @Autowired
    private SiLuHttpConfig httpConfig;

    @Autowired
    @Qualifier("siLuDorisTemplate")
    private JdbcTemplate siLuDorisTemplate;

    @Autowired
    private MinioService minioService;

    private Map<String, String> params = new HashMap<>();

    /**
     * 特殊作业
     */
    //region
    public String updateSecSpecialJobTicketFire(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_fire where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_fire";

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
                List<SecSpecialJobTicketFireDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketFireDto.class));
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

    public String updateSecSpecialJobTicketSpace(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `spaceName`, `spaceMedium`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_space where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_space";

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
                List<SecSpecialJobTicketSpaceDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketSpaceDto.class));
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

    public String updateSecSpecialJobTicketBlindplate(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `blindPlateType`, `pipingName`, `pipingMedium`, `pipingTem`, `pipingPressure`, `plateMaterial`, `plateSpecifications`, `plateCode`, `plateImg`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_blindplate where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_blindplate";

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
                List<SecSpecialJobTicketBlindplateDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketBlindplateDto.class));
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

    public String updateSecSpecialJobTicketHigh(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `highLevel`, `highHeight`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_high where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_high";

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
                List<SecSpecialJobTicketHighDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketHighDto.class));
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

    public String updateSecSpecialJobTicketHoising(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `hoistingLevel`, `hoistingLocation`, `spreaderName`, `suspendedName`, `sommer`, `commander`, `hoistingWeight`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_hoising where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_hoising";

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
                List<SecSpecialJobTicketHoisingDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketHoisingDto.class));
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

    public String updateSecSpecialJobTicketPower(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `powerStrategy`, `workVoltage`, `electricalEquipment`, `headCode`, `personCode`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_power where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_power";

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
                List<SecSpecialJobTicketPowerDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketPowerDto.class));
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

    public String updateSecSpecialJobTicketSoil(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `workImg`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_soil where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_soil";

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
                List<SecSpecialJobTicketSoilDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketSoilDto.class));
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

    public String updateSecSpecialJobTicketBreak(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `breakReason`, `involveUnit`, `breakImg`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_break where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_break";

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

    public String updateSecSpecialJobTicketPourback(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_pourback where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_pourback";

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
                List<SecSpecialJobTicketPourbackDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketPourbackDto.class));
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

    public String updateSecSpecialJobTicketPour(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_pour where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_pour";

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
                List<SecSpecialJobTicketPourDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketPourDto.class));
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

    public String updateSecSpecialJobTicketWater(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_water where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_water";

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
                List<SecSpecialJobTicketWaterDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketWaterDto.class));
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

    public String updateSecSpecialJobTicketMaintenance(String firstFlag) {
        boolean whileFlag = true;
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String querySql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                "from ythg_ods.dwd_sec_special_job_ticket_maintenance where deleted = '0' ";
        //String queryLatestDateTimeSql = "select createDate from ythg_ods.dwd_sec_deactivated_maintenance_record order by createDate desc limit 1";
        String path = "/sec_special_job_ticket_maintenance";

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
                List<SecSpecialJobTicketMaintenanceDto> list = siLuDorisTemplate.query(sql, new BeanPropertyRowMapper<>(SecSpecialJobTicketMaintenanceDto.class));
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

    private String toJsonBody() {
        return JSON.toJSONString(params);
    }
    /*public String updateSecSpecialTicketFile(String firstFlag) {
        List<String> ticketFileNames;
        List<SecSpecialJobTicketFileInfo> ticketInfo = new ArrayList<>();
        Map<String, String> tickets = new HashMap<>();
        if ("1".equals(firstFlag)) {
            tickets = minioService.getAllObjects();
        } else {
            tickets.putAll(minioService.getObjects(DateTimeUtil.getYesterdayBeginTime().substring(0, 9)));
            tickets.putAll(minioService.getObjects(LocalDateTime.now().toString().substring(0, 9)));
        }
        if (!tickets.isEmpty()) {
            ticketFileNames = new ArrayList<>(tickets.keySet());
            ticketFileNames.forEach(name -> {
                SecSpecialJobTicketFileInfo fileInfo = new SecSpecialJobTicketFileInfo();
                fileInfo.setFileName(name);
                if (45 == name.lastIndexOf("-")) {
                    fileInfo.setId(name.substring(0, 32));
                    fileInfo.setTimestamp(Long.valueOf(name.substring(32, 45)));
                    ticketInfo.add(fileInfo);
                } else {
                    fileInfo.setId(name.substring(0, 36));
                    fileInfo.setTimestamp(Long.valueOf(name.substring(36, 49)));
                    ticketInfo.add(fileInfo);
                }
            });
            Map<String, SecSpecialJobTicketFileInfo> latestObjects = ticketInfo.stream()
                    .collect(Collectors.toMap(
                            SecSpecialJobTicketFileInfo::getId,
                            Function.identity(),
                            (existing, replacement) -> existing.getTimestamp() > replacement.getTimestamp() ? existing : replacement
                    ));
            List<SecSpecialJobTicketFileInfo> latestFileInfo = new ArrayList<>(latestObjects.values());
            Map<String, String> finalTickets = tickets;
            latestFileInfo.forEach(info -> {
                String allSql = "select `id`, ticket_type as `ticketType`, 1 as `timestamp`, 'fileName' as `fileName` " +
                        "from dwd_sec_special_job_ticket where id = '" + info.getId() + "'";
                SecSpecialJobTicketFileInfo fileInfo = siLuDorisTemplate.queryForObject(allSql, SecSpecialJobTicketFileInfo.class);
                if (null != fileInfo) {
                    String tableName = TicketFileTypeEnum.getNameFromType(fileInfo.getTicketType());
                    String path = "/" + tableName;
                    String sql;
                    String datas;
                    switch (fileInfo.getTicketType()) {
                        case "01":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "02":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `spaceName`, `spaceMedium`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "03":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `blindPlateType`, `pipingName`, `pipingMedium`, `pipingTem`, `pipingPressure`, `plateMaterial`, `plateSpecifications`, `plateCode`, `plateImg`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "04":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `commitmentLetter`, `highLevel`, `highHeight`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "05":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "06":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "07":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "08":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "09":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "10":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "11":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;
                        case "12":
                            sql = "select `id`, `companyCode`, `ticketNo`, `ticketStatus`, `issueTime`, `planStartTime`, `planEndTime`, `startTime`, `endTime`, `ticketPosition`, `workAreaCode`, `ticketContent`, `checkedTime`, `checkedPerson`, `longitude`, `latitude`, `supervisorName`, `workerName`, `workDeptment`, `isContractorWork`, `contractorOrg`, `isAssociation`, `associationTicket`, `riskIdentification`, `disclosePerson`, `acceptPerson`, `tickerResponsName`, `majorPersonMobile`, `isChanged`, `changedCause`, `isCancelled`, `cancelledCause`, `mobileDeviceCode`, `workTicketAtt`, `safeDiscloseAtt`, `countersignImg`, `gasAnalysis`, `commitmentLetter`, `fireLocation`, `fireLevel`, `firePerson`, `fireStyle`, `deleted`, `createDate`, `createBy`, `updateDate`, `updateBy`, `ticket_level` " +
                                    "from ythg_ods.dwd_" + tableName + " where deleted = '0' and `id` = '" + fileInfo.getId() + "'";
                            log.info("<=====================sql语句==============================>");
                            log.info(sql);
                            SecSpecialJobTicketFireDto secSpecialJobTicketFireDto = siLuDorisTemplate.queryForObject(sql, SecSpecialJobTicketFireDto.class);
                            secSpecialJobTicketFireDto.setWorkTicketAtt(finalTickets.get(fileInfo.getFileName()));
                            datas = JSON.toJSONString(Arrays.asList(secSpecialJobTicketFireDto));
                            sendGetRequest(path, datas);
                            break;

                    }

                }
            });
            return "更新作业票：" + latestFileInfo;
        }
        return "更新作业票：0";
    }*/
    private void sendGetRequest(String path, String datas) {
        try{
            //组装请求
            params.put("datas", AESUtils.encrypt(datas));
            //调用API发送数据
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
            log.info(response);
        } catch (Exception e) {
            log.error("更新失败:", e);
        }
    }
}