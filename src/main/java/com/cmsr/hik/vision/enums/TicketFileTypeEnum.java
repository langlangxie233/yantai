package com.cmsr.hik.vision.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 删除标志
 *
 * @author wangyuting
 * @date 2021/4/28
 */
@Getter
public enum TicketFileTypeEnum {
    /**
     * 动火
     */
    TICKET_TYPE_01("01", "sec_special_job_ticket_fire"),

    /**
     * 受限空间
     */
    TICKET_TYPE_02("02", "sec_special_job_ticket_space"),
    /**
     * 盲板抽堵
     */
    TICKET_TYPE_03("03", "sec_special_job_ticket_ blindplate"),

    /**
     * 高处
     */
    TICKET_TYPE_04("04", "sec_special_job_ticket_high"),
    /**
     * 吊装
     */
    TICKET_TYPE_05("05", "sec_special_job_ticket_hoising"),

    /**
     * 临时用电
     */
    TICKET_TYPE_06("06", "sec_special_job_ticket_power"),
    /**
     * 动土
     */
    TICKET_TYPE_07("07", "sec_special_job_ticket_soil"),

    /**
     * 断路
     */
    TICKET_TYPE_08("08", "sec_special_job_ticket_break"),
    /**
     * 倒灌
     */
    TICKET_TYPE_09("09", "sec_special_job_ticket_pourback"),

    /**
     * 倾灌
     */
    TICKET_TYPE_10("10", "sec_special_job_ticket_pour"),
    /**
     * 切水
     */
    TICKET_TYPE_11("11", "sec_special_job_ticket_water"),

    /**
     * 检维修
     */
    TICKET_TYPE_12("12", "sec_special_job_ticket_maintenance");
    /**
     * 作业票类型
     */
    private String type;

    /**
     * 数据库名/接口名
     */
    private String name;

    /**
     * 数据删除标志枚举
     *
     * @param type 删除状态
     * @param name    描述
     */
    TicketFileTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getNameFromType(String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        for (TicketFileTypeEnum e : TicketFileTypeEnum.values()) {
            if (e.getType().equals(type)) {
                return e.getName();
            }
        }
        return null;
    }
}
