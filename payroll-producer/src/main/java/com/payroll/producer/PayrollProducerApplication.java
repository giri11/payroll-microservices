package com.payroll.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayrollProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayrollProducerApplication.class, args);
    }
}
