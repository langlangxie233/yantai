package com.cmsr.hik.vision.service;

import com.alibaba.fastjson2.JSON;
import com.cmsr.hik.vision.config.HikVisionConfig;
import com.cmsr.hik.vision.model.parking.ParkingHisRecord;
import com.cmsr.hik.vision.model.parking.ParkingInfo;
import com.cmsr.hik.vision.model.parking.Resp;
import com.cmsr.hik.vision.utils.DateTimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Service
public class HikVisionService {
    private static final String ARTEMIS_PATH = "/artemis";

    @Autowired
    private HikVisionConfig config;

    @Autowired
    @Qualifier("dorisTemplate")
    private JdbcTemplate dorisTemplate;

    private Map<String, String> params = new HashMap<>();

    public Resp<ParkingInfo> requestParkingInfo() {
        final String requestPath = ARTEMIS_PATH + "/api/pms/v1/tempCarInRecords/page";
        String respStr = doPost(requestPath);
        log.error("<===============停车库在库数量信息=====================>");
        log.error(respStr);
        Resp<ParkingInfo> resp = new Resp<>();
        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析JSON字符串到Resp<ParkingInfo>对象
            resp = objectMapper.readValue(respStr, new Resp<ParkingInfo>().getClass());

        } catch (Exception e) {
            log.error("Error exchange responce:", e.getMessage());
        }
        return resp;
    }

    public Resp<ParkingHisRecord> requestParkingRecord() {
        final String requestPath = ARTEMIS_PATH + "/api/pms/v1/crossRecords/page";
        String respStr = doPost(requestPath);
        //log.info("<==============record结果===========================>");
        //log.info(respStr);
        Resp<ParkingHisRecord> resp = new Resp<>();
        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析JSON字符串到Resp<ParkingInfo>对象
            resp = objectMapper.readValue(respStr, new Resp<ParkingHisRecord>().getClass());

        } catch (Exception e) {
            log.error("Error exchange responce:", e.getMessage());
        }
        return resp;
    }

    private String doPost(String path) {
        try {
            //log.info("config:" + config.toString());
            //log.info("path:" + path);
            //log.info("body:" + toJsonBody());
            String response = ArtemisHttpUtil.doPostStringArtemis(
                    config.getConfig(),
                    toPathMap(path),
                    toJsonBody(),
                    null,
                    null,
                    MediaType.APPLICATION_JSON_VALUE
            );
            //log.info("Get Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error occurred, while requesting hik-vision: {}" + e.getMessage() + "CausedBy://n" + e.getCause());
        }
        return null;
    }

    private String toJsonBody() {
        return JSON.toJSONString(params);
    }

    private Map<String, String> toPathMap(String path) {
        Map<String, String> pathMap = Maps.newHashMap();
        pathMap.put("https://", path);
        return pathMap;
    }

    public void updateParkingInfo(Integer pageNo, Integer pageSize) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pageNo", pageNo.toString());
        queryParams.put("pageSize", pageSize.toString());
        setParams(queryParams);
        Resp<ParkingInfo> data = requestParkingInfo();
        if (null != data) {
            if(null != data.getData() && null != data.getData().getTotal()) {
                Integer using = data.getData().getTotal();
                int result = 0;
                String deleteSql="delete from hikvision.partition_info where `id` = 1";
                String sql="insert into hikvision.partition_info (`id`, `using`) values (1, " + using + ")";
                try {
                    dorisTemplate.update(deleteSql);
                    result = dorisTemplate.update(sql);
                } catch (Exception e) {
                    log.error("更新失败:", e);
                }
                if ( result > 0) {
                    log.info("停车厂信息更新成功");
                }
            } else {
                log.info("未查询到数据：" + data.getMsg());
            }
        } else {
            log.info("查询sdk错误");
        }
    }

    public void updateParkingRecord(Integer pageNo, Integer pageSize) {
        Map<String, String> queryParams = new HashMap<>();
        String querySql = "select create_time from hikvision.partition_record order by create_time desc limit 1";
        //LocalDateTime startTime = dorisTemplate.queryForObject(querySql, LocalDateTime.class);
        List<String> dateTimeList = dorisTemplate.queryForList(querySql, String.class);
        //List<String> dateTimeList = new ArrayList<>();
        dateTimeList.add("2024-01-01 00:00:00");
        if (!dateTimeList.isEmpty()) {
            //log.info("数据库中最近更新时间：" + dateTimeList.get(0));
            queryParams.put("startTime", DateTimeUtil.dateFormat(dateTimeList.get(0)));
        } else {
            queryParams.put("startTime", "2024-01-01T00:00:00+08:00");
            log.warn("数据库中暂无数据");
        }
        while (true) {
            queryParams.put("pageNo", pageNo.toString());
            queryParams.put("pageSize", pageSize.toString());
            setParams(queryParams);
            try {
                Resp<ParkingHisRecord> records = requestParkingRecord();
                //test数据
                if (null != records){
                    if(null != records.getData() && null != records.getData().getList() && !records.getData().getList().isEmpty()){

                        String sql="insert into hikvision.partition_record " +
                                "(cross_record_syscode, park_syscode, park_name, entrance_syscode, entrance_name, roadway_syscode, roadway_name, vehicle_out, release_mode, release_result, release_way, release_reason, plate_no, card_no, vehicle_color, vehicle_type, plate_color, plate_type, car_category, car_category_name, vehicle_pic_uri, plate_no_pic_uri, face_pic_uri, asw_syscode, cross_time, create_time) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        List<ParkingHisRecord> list = JSON.parseArray(JSON.toJSONString(records.getData().getList()), ParkingHisRecord.class);
                        dorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, list.get(i).getCrossRecordSyscode());
                                ps.setObject(2, list.get(i).getParkSyscode());
                                ps.setObject(3, list.get(i).getParkName());
                                ps.setObject(4, list.get(i).getEntranceSyscode());
                                ps.setObject(5, list.get(i).getEntranceName());
                                ps.setObject(6, list.get(i).getRoadwaySyscode());
                                ps.setObject(7, list.get(i).getRoadwayName());
                                ps.setObject(8, list.get(i).getVehicleOut());
                                ps.setObject(9, list.get(i).getReleaseMode());
                                ps.setObject(10, list.get(i).getReleaseResult());
                                ps.setObject(11, list.get(i).getReleaseWay());
                                ps.setObject(12, list.get(i).getReleaseReason());
                                ps.setObject(13, list.get(i).getPlateNo());
                                ps.setObject(14, list.get(i).getCardNo());
                                ps.setObject(15, list.get(i).getVehicleColor());
                                ps.setObject(16, list.get(i).getVehicleType());
                                ps.setObject(17, list.get(i).getPlateColor());
                                ps.setObject(18, list.get(i).getPlateType());
                                ps.setObject(19, list.get(i).getCarCategory());
                                ps.setObject(20, list.get(i).getCarCategoryName());
                                ps.setObject(21, list.get(i).getVehiclePicUri());
                                ps.setObject(22, list.get(i).getPlateNoPicUri());
                                ps.setObject(23, list.get(i).getFacePicUri());
                                ps.setObject(24, list.get(i).getAswSyscode());
                                ps.setObject(25, list.get(i).getCrossTime());
                                ps.setObject(26, list.get(i).getCreateTime());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return list.size();
                            }
                        });
                    } else {
                        log.error("未查询到数据:" + records.getMsg());
                    }
                } else {
                    log.error("查询sdk错误");
                    break;
                }
                if (records.getData().getPageNo() * records.getData().getPageSize() >= records.getData().getTotal()) {
                    break;
                } else {
                    pageNo++;
                }
            } catch (Exception e) {
                log.error("更新失败:", e);
                break;
            }
        }
    }
}
