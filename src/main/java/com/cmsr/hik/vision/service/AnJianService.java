package com.cmsr.hik.vision.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.cmsr.hik.vision.config.anjian.AnJianHttpConfig;
import com.cmsr.hik.vision.model.anjian.camera.CameraVo;
import com.cmsr.hik.vision.model.anjian.camera.ViolationInfo;
import com.cmsr.hik.vision.model.anjian.result.GetTokenResponse;
import com.cmsr.hik.vision.utils.DateTimeUtil;
import com.cmsr.hik.vision.utils.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安监对接
 *
 * @author 上研院 xiexianlang
 * @date 2024/5/7 15:29
 */
@Data
@Slf4j
@Service
public class AnJianService {

    @Autowired
    private AnJianHttpConfig httpConfig;

    @Autowired
    @Qualifier("anJianDorisTemplate")
    private JdbcTemplate anJianDorisTemplate;

    private Map<String, String> params = new HashMap<>();

    /**
     * 更新违规列表
     *
     * @return 更新结果
     */
    public String updateQueryViolationList() {
        //初始化
        boolean whileFlag = true;
        params.clear();
        int pageNo = 1;
        int pageSize = 50;
        List<ViolationInfo> inserts = new ArrayList<>();
        List<ViolationInfo> updates = new ArrayList<>();
        String isFirst = "select `id` from anjian.violation_info";
        if (0 != anJianDorisTemplate.queryForList(isFirst).size()) {
            params.put("beginTime", DateTimeUtil.getMinutesBefore(6));
        }
        //调用API发送数据
        while (whileFlag) {
            try {
                String path = "/video/v1.0.0/violation";
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                //log.info("token:" + token);
                headers.put("Content-Type", "application/json");
                headers.put("User-Agent", "google");
                headers.put("authorization", token);
                params.put("current", String.valueOf(pageNo));
                params.put("size", String.valueOf(pageSize));
                String jsonBody = toJsonBody();
                String url = httpConfig.getUrl() + path;
                log.info("url：" + url);
                //log.info("header：" + headers.toString());
                //log.info("更新违规列表jsonBody：" + jsonBody);
                String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                log.info("违规列表response：" + response);
                JSONObject res = JSONObject.parseObject(response);
                Integer pages = res.getJSONObject("data").getJSONObject("data").getInteger("pages");
                List<ViolationInfo> records = JSONArray.parseArray(res.getJSONObject("data").getJSONObject("data").get("records").toString(), ViolationInfo.class);
                if (!records.isEmpty()) {
                    records.forEach(l -> {
                        String repeatSql = "select `id` from anjian.violation_info where `id` = '" + l.getId() + "'";
                        if (0 == anJianDorisTemplate.queryForList(repeatSql).size()) {
                            inserts.add(l);
                        } else {
                            updates.add(l);
                        }
                    });
                    //入库
                    if (!inserts.isEmpty()) {
                        log.info("inserts:" + inserts.size());
                        //log.info("inserts:" + inserts.get(0).toString());
                        String sql="insert into anjian.violation_info " +
                                "(`id`,`metadata`,`superUserId`,`message`,`userName`,`handleUserId`,`recordFps`,`picPath`,`cameraId`,`violationName`,`addressName`,`recognitionName`,`recordPath`,`cameraName`,`status`,`updatedAt`,`createdAt`,`recordDuration`) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        anJianDorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, inserts.get(i).getId());
                                ps.setObject(2, inserts.get(i).getMetadata());
                                ps.setObject(3, inserts.get(i).getSuperUserId());
                                ps.setObject(4, inserts.get(i).getMessage());
                                ps.setObject(5, inserts.get(i).getUserName());
                                ps.setObject(6, inserts.get(i).getHandleUserId());
                                ps.setObject(7, inserts.get(i).getRecordFps());
                                ps.setObject(8, inserts.get(i).getPicPath());
                                ps.setObject(9, inserts.get(i).getCameraId());
                                ps.setObject(10, inserts.get(i).getViolationName());
                                ps.setObject(11, inserts.get(i).getAddressName());
                                ps.setObject(12, inserts.get(i).getRecognitionName());
                                ps.setObject(13, inserts.get(i).getRecordPath());
                                ps.setObject(14, inserts.get(i).getCameraName());
                                ps.setObject(15, inserts.get(i).getStatus());
                                ps.setObject(16, inserts.get(i).getUpdatedAt());
                                ps.setObject(17, inserts.get(i).getCreatedAt());
                                ps.setObject(18, inserts.get(i).getRecordDuration());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return inserts.size();
                            }
                        });
                        inserts.clear();
                    }
                    if (!updates.isEmpty()) {
                        log.info("updates:" + updates.size());
                        String sql="update anjian.violation_info " +
                                "set `metadata`=?,`superUserId`=?,`message`=?,`userName`=?,`handleUserId`=?,`recordFps`=?,`picPath`=?,`cameraId`=?,`violationName`=?,`addressName`=?,`recognitionName`=?,`recordPath`=?,`cameraName`=?,`status`=?,`updatedAt`=?,`createdAt`=?,`recordDuration`=? " +
                                "where `id`=?";
                        anJianDorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, updates.get(i).getMetadata());
                                ps.setObject(2, updates.get(i).getSuperUserId());
                                ps.setObject(3, updates.get(i).getMessage());
                                ps.setObject(4, updates.get(i).getUserName());
                                ps.setObject(5, updates.get(i).getHandleUserId());
                                ps.setObject(6, updates.get(i).getRecordFps());
                                ps.setObject(7, updates.get(i).getPicPath());
                                ps.setObject(8, updates.get(i).getCameraId());
                                ps.setObject(9, updates.get(i).getViolationName());
                                ps.setObject(10, updates.get(i).getAddressName());
                                ps.setObject(11, updates.get(i).getRecognitionName());
                                ps.setObject(12, updates.get(i).getRecordPath());
                                ps.setObject(13, updates.get(i).getCameraName());
                                ps.setObject(14, updates.get(i).getStatus());
                                ps.setObject(15, updates.get(i).getUpdatedAt());
                                ps.setObject(16, updates.get(i).getCreatedAt());
                                ps.setObject(17, updates.get(i).getRecordDuration());
                                ps.setObject(18, updates.get(i).getId());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return updates.size();
                            }
                        });
                        updates.clear();
                    }

                    if (pageNo < pages) {
                        pageNo++;
                        headers.clear();
                        params.put("current", String.valueOf(pageNo));
                    } else {
                        whileFlag = false;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.info("请求错误：" + e);
                whileFlag = false;
            }
        }
        return null;
    }

    /**
     * 更新违规状态列表
     *
     * @return 更新结果
     */
    public String updateQueryViolationStatusList() {
        //初始化
        boolean whileFlag = true;
        params.clear();
        int pageNo = 1;
        int pageSize = 50;
        List<ViolationInfo> updates = new ArrayList<>();
        params.put("beginTime", DateTimeUtil.getDaysBefore(7));
        //调用API发送数据
        while (whileFlag) {
            try {
                String path = "/video/v1.0.0/violation";
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                //log.info("token:" + token);
                headers.put("Content-Type", "application/json");
                headers.put("User-Agent", "google");
                headers.put("authorization", token);
                params.put("current", String.valueOf(pageNo));
                params.put("size", String.valueOf(pageSize));
                params.put("status", "1,2");
                String jsonBody = toJsonBody();
                String url = httpConfig.getUrl() + path;
                log.info("url：" + url);
                //log.info("header：" + headers.toString());
                //log.info("更新违规状态列表jsonBody：" + jsonBody.replace("\"1,2\"", "[1,2]"));
                String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody.replace("\"1,2\"", "[1,2]"));
                JSONObject res = JSONObject.parseObject(response);
                Integer pages = res.getJSONObject("data").getJSONObject("data").getInteger("pages");
                List<ViolationInfo> records = JSONArray.parseArray(res.getJSONObject("data").getJSONObject("data").get("records").toString(), ViolationInfo.class);
                if (!records.isEmpty()) {
                    records.forEach(l -> {
                        String repeatSql = "select `id` from anjian.violation_info where `id` = '" + l.getId() + "'";
                        if (0 != anJianDorisTemplate.queryForList(repeatSql).size()) {
                            updates.add(l);
                        }
                    });
                    //入库
                    if (!updates.isEmpty()) {
                        //log.info("updates:" + updates.size());
                        String sql="update anjian.violation_info " +
                                "set `metadata`=?,`superUserId`=?,`message`=?,`userName`=?,`handleUserId`=?,`recordFps`=?,`picPath`=?,`cameraId`=?,`violationName`=?,`addressName`=?,`recognitionName`=?,`recordPath`=?,`cameraName`=?,`status`=?,`updatedAt`=?,`createdAt`=?,`recordDuration`=? " +
                                "where `id`=?";
                        anJianDorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, updates.get(i).getMetadata());
                                ps.setObject(2, updates.get(i).getSuperUserId());
                                ps.setObject(3, updates.get(i).getMessage());
                                ps.setObject(4, updates.get(i).getUserName());
                                ps.setObject(5, updates.get(i).getHandleUserId());
                                ps.setObject(6, updates.get(i).getRecordFps());
                                ps.setObject(7, updates.get(i).getPicPath());
                                ps.setObject(8, updates.get(i).getCameraId());
                                ps.setObject(9, updates.get(i).getViolationName());
                                ps.setObject(10, updates.get(i).getAddressName());
                                ps.setObject(11, updates.get(i).getRecognitionName());
                                ps.setObject(12, updates.get(i).getRecordPath());
                                ps.setObject(13, updates.get(i).getCameraName());
                                ps.setObject(14, updates.get(i).getStatus());
                                ps.setObject(15, updates.get(i).getUpdatedAt());
                                ps.setObject(16, updates.get(i).getCreatedAt());
                                ps.setObject(17, updates.get(i).getRecordDuration());
                                ps.setObject(18, updates.get(i).getId());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return updates.size();
                            }
                        });
                        updates.clear();
                    }

                    if (pageNo < pages) {
                        pageNo++;
                        headers.clear();
                        params.put("current", String.valueOf(pageNo));
                    } else {
                        whileFlag = false;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.info("请求错误：" + e);
                whileFlag = false;
            }
        }
        return null;
    }

    /**
     * 更新摄像头信息列表
     *
     * @return 更新结果
     */
    public String updateQueryCameraList() {
        //初始化
        boolean whileFlag = true;
        params.clear();
        int pageNo = 1;
        int pageSize = 50;
        List<CameraVo> inserts = new ArrayList<>();
        List<CameraVo> updates = new ArrayList<>();
        //调用API发送数据
        while (whileFlag) {
            try {
                String path = "/video/v1.0.0/camera/page";
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                //log.info("token:" + token);
                headers.put("Content-Type", "application/json");
                headers.put("User-Agent", "google");
                headers.put("authorization", token);
                params.put("current", String.valueOf(pageNo));
                params.put("size", String.valueOf(pageSize));
                //headers.put("Accept", "application/json");
                //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
                String jsonBody = toJsonBody();
                String url = httpConfig.getUrl() + path;
                log.info("url：" + url);
                //log.info("header：" + headers.toString());
                //log.info("jsonBody：" + jsonBody);
                String response = HttpClientUtil.sendPostRequest(url, headers, jsonBody);
                JSONObject res = JSONObject.parseObject(response);
                Integer pages = res.getJSONObject("data").getJSONObject("data").getInteger("pages");
                List<CameraVo> records = JSONArray.parseArray(res.getJSONObject("data").getJSONObject("data").get("records").toString(), CameraVo.class);
                if (!records.isEmpty()) {
                    records.forEach(l -> {
                        String repeatSql = "select `id` from anjian.camera_info where `id` = '" + l.getId() + "'";
                        if (0 == anJianDorisTemplate.queryForList(repeatSql).size()) {
                            inserts.add(l);
                        } else {
                            updates.add(l);
                        }
                    });
                    //入库
                    if (!inserts.isEmpty()) {
                        //log.info("inserts:" + inserts.size());
                        String sql="insert into anjian.camera_info " +
                                "(`id`,`picturePath`,`deviceIp`,`isCanControl`,`groupsId`,`devicePwd`,`deviceChannelList`,`englishName`,`groupsName`,`urlDirect`,`recognitions`,`type`,`roi`,`deviceId`,`deviceUser`,`flv`,`hls`,`addressId`,`catalogId`,`thirdDeviceId`,`thirdChannelId`,`rtspPort`,`name`,`updatedTime`,`createdTime`,`addressName`,`tenantId`,`devicePort`,`fence`,`status`) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        anJianDorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, inserts.get(i).getId());
                                ps.setObject(2, inserts.get(i).getPicturePath());
                                ps.setObject(3, inserts.get(i).getDeviceIp());
                                ps.setObject(4, inserts.get(i).getIsCanControl());
                                ps.setObject(5, inserts.get(i).getGroupsId());
                                ps.setObject(6, inserts.get(i).getDevicePwd());
                                ps.setObject(7, inserts.get(i).getDeviceChannelList());
                                ps.setObject(8, inserts.get(i).getEnglishName());
                                ps.setObject(9, inserts.get(i).getGroupsName());
                                ps.setObject(10, inserts.get(i).getUrlDirect());
                                ps.setObject(11, inserts.get(i).getRecognitions());
                                ps.setObject(12, inserts.get(i).getType());
                                ps.setObject(13, inserts.get(i).getRoi());
                                ps.setObject(14, inserts.get(i).getDeviceId());
                                ps.setObject(15, inserts.get(i).getDeviceUser());
                                ps.setObject(16, inserts.get(i).getUrl().getFlv());
                                ps.setObject(17, inserts.get(i).getUrl().getHls());
                                ps.setObject(18, inserts.get(i).getAddressId());
                                ps.setObject(19, inserts.get(i).getCatalogId());
                                ps.setObject(20, inserts.get(i).getThirdDeviceId());
                                ps.setObject(21, inserts.get(i).getThirdChannelId());
                                ps.setObject(22, inserts.get(i).getRtspPort());
                                ps.setObject(23, inserts.get(i).getName());
                                ps.setObject(24, inserts.get(i).getUpdatedTime());
                                ps.setObject(25, inserts.get(i).getCreatedTime());
                                ps.setObject(26, inserts.get(i).getAddressName());
                                ps.setObject(27, inserts.get(i).getTenantId());
                                ps.setObject(28, inserts.get(i).getDevicePort());
                                ps.setObject(29, inserts.get(i).getFence());
                                ps.setObject(30, inserts.get(i).getStatus());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return inserts.size();
                            }
                        });
                        inserts.clear();
                    }
                    if (!updates.isEmpty()) {
                        //log.info("updates:" + updates.size());
                        String sql="update anjian.camera_info " +
                                "set `status`=?,`picturePath`=?,`deviceIp`=?,`isCanControl`=?,`groupsId`=?,`devicePwd`=?,`deviceChannelList`=?,`englishName`=?,`groupsName`=?,`urlDirect`=?,`recognitions`=?,`type`=?,`roi`=?,`deviceId`=?,`deviceUser`=?,`flv`=?,`hls`=?,`addressId`=?,`catalogId`=?,`thirdDeviceId`=?,`thirdChannelId`=?,`rtspPort`=?,`name`=?,`updatedTime`=?,`createdTime`=?,`addressName`=?,`tenantId`=?,`devicePort`=?,`fence`=? " +
                                "where `id`=?";
                        anJianDorisTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setObject(1, updates.get(i).getStatus());
                                ps.setObject(2, updates.get(i).getPicturePath());
                                ps.setObject(3, updates.get(i).getDeviceIp());
                                ps.setObject(4, updates.get(i).getIsCanControl());
                                ps.setObject(5, updates.get(i).getGroupsId());
                                ps.setObject(6, updates.get(i).getDevicePwd());
                                ps.setObject(7, updates.get(i).getDeviceChannelList());
                                ps.setObject(8, updates.get(i).getEnglishName());
                                ps.setObject(9, updates.get(i).getGroupsName());
                                ps.setObject(10, updates.get(i).getUrlDirect());
                                ps.setObject(11, updates.get(i).getRecognitions());
                                ps.setObject(12, updates.get(i).getType());
                                ps.setObject(13, updates.get(i).getRoi());
                                ps.setObject(14, updates.get(i).getDeviceId());
                                ps.setObject(15, updates.get(i).getDeviceUser());
                                ps.setObject(16, updates.get(i).getUrl().getFlv());
                                ps.setObject(17, updates.get(i).getUrl().getHls());
                                ps.setObject(18, updates.get(i).getAddressId());
                                ps.setObject(19, updates.get(i).getCatalogId());
                                ps.setObject(20, updates.get(i).getThirdDeviceId());
                                ps.setObject(21, updates.get(i).getThirdChannelId());
                                ps.setObject(22, updates.get(i).getRtspPort());
                                ps.setObject(23, updates.get(i).getName());
                                ps.setObject(24, updates.get(i).getUpdatedTime());
                                ps.setObject(25, updates.get(i).getCreatedTime());
                                ps.setObject(26, updates.get(i).getAddressName());
                                ps.setObject(27, updates.get(i).getTenantId());
                                ps.setObject(28, updates.get(i).getDevicePort());
                                ps.setObject(29, updates.get(i).getFence());
                                ps.setObject(30, updates.get(i).getId());
                            }
                            @Override
                            public int getBatchSize() {
                                //返回要插入的行数
                                return updates.size();
                            }
                        });
                        updates.clear();
                    }

                    if (pageNo < pages) {
                        pageNo++;
                        headers.clear();
                        params.put("current", String.valueOf(pageNo));
                    } else {
                        whileFlag = false;
                    }
                } else {
                    log.info("暂无更新数据");
                    return "暂无更新数据";
                }
            } catch (Exception e) {
                log.info("请求错误：" + e);
                whileFlag = false;
            }
        }
        return "更新成功";
    }

    private String toJsonBody() {
        return JSON.toJSONString(params);
    }

    private String getToken() {
        GetTokenResponse tokenResponse = new GetTokenResponse();
        String path = "/custom/v1.0.0/register/sso-login";
        Map<String, String> bodys = new HashMap<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("User-Agent", "google");
            //headers.put("Authorization", "Basic " + Base64.encode((list.get(0).getCompanyCode() + ':' + "code").getBytes()));
            bodys.put("mobilePhone", httpConfig.getMobilePhone());
            bodys.put("key", httpConfig.getKey());
            bodys.put("secret", httpConfig.getSecret());
            String url = httpConfig.getUrl() + path;
            log.info("url：" + url);
            //log.info("header：" + headers.toString());
            //log.info("jsonBody：" + jsonBody);

            String response = HttpClientUtil.sendPostRequest(url, headers, JSON.toJSONString(bodys));
            //log.info("token response" + response);
            tokenResponse = JSON.to(GetTokenResponse.class, response);
        } catch (Exception e) {
            log.info("获取token错误：" + e);
        }
        return tokenResponse.getData().getData().getAuthorization();
    }

}
