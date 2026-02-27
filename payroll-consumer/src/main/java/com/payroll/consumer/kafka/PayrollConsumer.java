package com.payroll.consumer.kafka;

import com.payroll.consumer.dto.PayrollData;
import com.payroll.consumer.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollConsumer {
    
    private final PayrollService payrollService;
    
    @KafkaListener(
            topics = "${spring.kafka.topic.payroll-data}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePayrollData(
            @Payload PayrollData payrollData,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  [CONSUMER] 📨 Received message from Kafka                   ║");
        log.info("║  Employee ID: {}                                        ║", payrollData.getEmployeeId());
        log.info("║  Partition: {}  |  Offset: {}                              ║", partition, offset);
        log.info("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            payrollService.processPayrollData(payrollData);
            log.info("[CONSUMER] ✅ Successfully processed payroll data for Employee ID: {}", 
                    payrollData.getEmployeeId());
        } catch (Exception e) {
            log.error("[CONSUMER] ❌ Error processing payroll data for Employee ID: {}", 
                    payrollData.getEmployeeId(), e);
            // Implement retry logic or dead letter queue here if needed
        }
    }
}
