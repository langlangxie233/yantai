package com.cmsr.hik.vision.model.silu.specialassignments;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上报动火作业票
 *
 * @author 上研院 xiexianlang
 * @date 2024/4/24 14:02
 */
@Setter
@Getter
public class SecSpecialJobTicketItemDto {
    private String id;
    private String bucketName;
    private Long timeStamp;
    private Byte[] file;

}
