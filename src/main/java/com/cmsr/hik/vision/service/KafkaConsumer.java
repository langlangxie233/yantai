package com.cmsr.hik.vision.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 * @author 上研院 xiexianlang
 * @date 2024/6/6 10:49
 */
@Slf4j
@ConditionalOnProperty(name = "silu.kafka.enable", havingValue = "true")
@Component
public class KafkaConsumer {
    @Autowired
    private SiLuService siLuService;

    @KafkaListener(topics = "sec_employee_real_loaction", groupId = "employee",
            containerFactory = "kafkaTwoContainerFactory")
    public void dxpEmployeeRealLocation(ConsumerRecord<String, String> record, Acknowledgment ack,
                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        consume(record, ack, topic, msg -> siLuService.updateSecEmployeeRealLocationInstance(msg));
    }

    @KafkaListener(topics = "sec_employee_alarm_data", groupId = "employee",
            containerFactory = "kafkaTwoContainerFactory")
    public void dxpEmployeeAlarmData(ConsumerRecord<String, String> record, Acknowledgment ack,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        consume(record, ack, topic, msg -> siLuService.updateSecEmployeeAlarmDataInstance(msg));
    }

    private void consume(ConsumerRecord<String, String> record, Acknowledgment ack, String topic,
                         java.util.function.Consumer<String> consumer) {
        Optional<String> optional = Optional.ofNullable(record.value());
        if (!optional.isPresent()) {
            log.warn("kafka收到消息 但为空，record:{}", record);
            return;
        }
        String msg = optional.get();
        log.info("kafka收到消息  开始消费 topic:{},msg:{}", topic, msg);
        try {
            consumer.accept(msg);
            // 上面方法执行成功后手动提交
            ack.acknowledge();
            log.info("kafka收到消息消费成功 topic:{},msg:{}", topic, msg);
        } catch (Exception e) {
            log.error("kafka消费消息失败 topic:{},msg:{}", topic, msg, e);
        }
    }
}