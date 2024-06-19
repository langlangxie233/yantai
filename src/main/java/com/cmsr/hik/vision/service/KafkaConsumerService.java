package com.cmsr.hik.vision.service;

import com.alibaba.fastjson2.JSON;
import com.cmsr.hik.vision.config.silu.SiLuHttpConfig;
import com.cmsr.hik.vision.model.silu.personnelpositioning.*;
import com.cmsr.hik.vision.model.silu.result.ResponseData;
import com.cmsr.hik.vision.model.silu.result.ResponseObject;
import com.cmsr.hik.vision.utils.AESUtils;
import com.cmsr.hik.vision.utils.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * kafka消费者
 *
 * @author 上研院 xiexianlang
 * @date 2024/6/6 10:49
 */
@Slf4j
@ConditionalOnProperty(name = "silu.kafka.enable", havingValue = "true")
@Component
public class KafkaConsumerService {
    @Autowired
    private SiLuHttpConfig httpConfig;

    private Map<String, String> params = new HashMap<>();

    @Autowired
    @Qualifier("siLuDorisTemplate")
    private JdbcTemplate siLuDorisTemplate;

    @KafkaListener(topics = "sec_employee_real_loaction", groupId = "employee",
            containerFactory = "kafkaTwoContainerFactory")
    public void dxpEmployeeRealLocation(List<String> record,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (!record.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<SecEmployeeRealLocationDto> result = new ArrayList<>();
                SecEmployeeRealLocationRecord realLocationRecord = objectMapper.readValue(record.toString().replace("[{", "{").replace("}]", "}"), SecEmployeeRealLocationRecord.class);
                log.error("解析Kafka成功");
                realLocationRecord.getData().forEach(datas -> {
                    SecEmployeeRealLocationDto dto = new SecEmployeeRealLocationDto();
                    dto.setImei(datas.getCard_no());
                    dto.setTime(datas.getTime_stamp());
                    dto.setLon(datas.getLongitude());
                    dto.setLat(datas.getLatitude());
                    String querySql = "select `id` from ythg_ods.dwd_sec_employee_file where imei = '"+ dto.getImei() + "' limit 1";
                    List<SecEmployeeFileDto> list = siLuDorisTemplate.query(querySql, new BeanPropertyRowMapper<>(SecEmployeeFileDto.class));
                    if (0 != list.size()) {
                        result.add(dto);
                    }
                });
                // 在这里你可以使用employeeRealLocation对象进行进一步的处理
                updateSecEmployeeRealLocation(result);
            } catch (Exception e) {
                log.error("解析Kafka消息为EmployeeRealLocation对象时出错record:{}", record.toString(), e);
            }
        }

//        consume(record, topic, msg -> siLuService.updateSecEmployeeRealLocationInstance(msg));
    }

    @KafkaListener(topics = "sec_employee_alarm_data", groupId = "employee",
            containerFactory = "kafkaTwoContainerFactory")
    public void dxpEmployeeAlarmData(List<String> record,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (!record.isEmpty()) {
            String recordString = record.toString().replace("[", "{").replace("]", "}");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<SecEmployeeAlarmDataDto> result = new ArrayList<>();
                SecEmployeeAlarmDataRecord alarmDataRecord = objectMapper.readValue(record.toString().replace("[{", "{").replace("}]", "}"), SecEmployeeAlarmDataRecord.class);
                log.error("解析Kafka成功");
                alarmDataRecord.getData().forEach(datas -> {
                    SecEmployeeAlarmDataDto dto = new SecEmployeeAlarmDataDto();
                    dto.setThirdId(datas.getId());
                    dto.setAlarmType(datas.getTime());
                    dto.setAlarmTime(datas.getType());
                    dto.setLocationCode(datas.getArea_name());
                    dto.setLongitude(datas.getName());
                    dto.setLatitude(datas.getCard_no());
                    dto.setAddress(datas.getHandle_time());
                    dto.setAlarmReason(datas.getHandle_remark());
                    dto.setAlarmSource(datas.getLongitude());
                    dto.setAlarmStatus(datas.getLatitude());
                    dto.setImage(datas.getCompany_social_code());
                    result.add(dto);
                });
                // 在这里你可以使用employeeRealLocation对象进行进一步的处理
                updateSecEmployeeAlarmData(result);
            } catch (Exception e) {
                log.error("解析Kafka消息为EmployeeRealLocation对象时出错record:{}", record.toString(), e);
            }
        }
//        consume(record, topic, msg -> siLuService.updateSecEmployeeAlarmDataInstance(msg));
    }

    public String updateSecEmployeeRealLocation(List<SecEmployeeRealLocationDto> list) {
        params.clear();
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String path = "/sec_employee_real_loaction";
        try{
            if (!list.isEmpty()) {
                //log.info("<====================数据库查到的数据===========================>");
                //log.info("list.size:" + list.size());
                //组装请求
                params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                //调用API发送数据
                try {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    String jsonBody = toJsonBody();
                    String url = httpConfig.getUrl() + path;
                    log.info("url：" + url);
                    String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                    responseObject = JSON.to(ResponseObject.class, response);
                    log.info(response);
                } catch (Exception e) {
                    log.info("请求错误：" + e);
                }
            } else {
                log.info("暂无更新数据");
                return "暂无更新数据";
            }
        } catch (Exception e) {
            log.error("更新失败:", e);
        }
        return responseObject.getData().getMsg();
    }

    public String updateSecEmployeeAlarmData(List<SecEmployeeAlarmDataDto> list) {
        params.clear();
        ResponseData responseData = new ResponseData();
        responseData.setMsg("更新失败");
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(responseData);
        String path = "/sec_employee_alarm_data";
        try{
            if (!list.isEmpty()) {
                log.info("<====================数据库查到的数据===========================>");
                log.info("list.size:" + list.size());
                //组装请求
                params.put("datas", AESUtils.encrypt(JSON.toJSONString(list)));
                //调用API发送数据
                try {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    String jsonBody = toJsonBody();
                    String url = httpConfig.getUrl() + path;
                    log.info("url：" + url);

                    String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                    responseObject = JSON.to(ResponseObject.class, response);
                    log.info(response);
                } catch (Exception e) {
                    log.info("请求错误：" + e);
                }
            } else {
                log.info("暂无更新数据");
                return "暂无更新数据";
            }
        } catch (Exception e) {
            log.error("更新失败:", e);
        }
        return responseObject.getData().getMsg();
    }

    private String toJsonBody() {
        return JSON.toJSONString(params);
    }
}