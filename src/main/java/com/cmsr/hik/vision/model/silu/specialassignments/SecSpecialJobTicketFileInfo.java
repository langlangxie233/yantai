package com.cmsr.hik.vision.model.silu.specialassignments;

import lombok.Getter;
import lombok.Setter;


/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSpecialJobTicketFileInfo {
    private String id;
    private Long timestamp;
    private String ticketType;
    private String fileName;

}
