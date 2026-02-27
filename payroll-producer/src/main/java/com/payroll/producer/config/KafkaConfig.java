package com.payroll.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.topic.payroll-data}")
    private String payrollTopicName;
    
    @Bean
    public NewTopic payrollTopic() {
        return TopicBuilder.name(payrollTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
