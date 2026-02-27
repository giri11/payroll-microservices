package com.payroll.producer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class ProducerController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("[PRODUCER] Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Payroll Producer");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.info("[PRODUCER] Status endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Payroll Producer");
        response.put("status", "RUNNING");
        response.put("description", "CSV file processor and Kafka producer");
        return ResponseEntity.ok(response);
    }
}
