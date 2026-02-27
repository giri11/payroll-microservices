package com.payroll.producer.kafka;

import com.payroll.producer.dto.PayrollData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollProducer {
    
    private final KafkaTemplate<String, PayrollData> kafkaTemplate;
    
    @Value("${spring.kafka.topic.payroll-data}")
    private String topicName;
    
    public void sendPayrollData(PayrollData payrollData) {
        log.info("[PRODUCER] Sending payroll data to Kafka topic: {} for employee: {}", 
                topicName, payrollData.getEmployeeId());
        
        CompletableFuture<SendResult<String, PayrollData>> future = 
                kafkaTemplate.send(topicName, payrollData.getEmployeeId(), payrollData);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[PRODUCER] ✅ Successfully sent message to Kafka: Employee ID={}, Offset={}, Partition={}", 
                        payrollData.getEmployeeId(),
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("[PRODUCER] ❌ Failed to send message to Kafka for Employee ID: {}", 
                        payrollData.getEmployeeId(), ex);
            }
        });
    }
}
