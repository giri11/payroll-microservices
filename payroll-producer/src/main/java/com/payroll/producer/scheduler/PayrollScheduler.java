package com.payroll.producer.scheduler;

import com.opencsv.exceptions.CsvException;
import com.payroll.producer.dto.PayrollData;
import com.payroll.producer.kafka.PayrollProducer;
import com.payroll.producer.service.CsvReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "payroll.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class PayrollScheduler {
    
    private final CsvReaderService csvReaderService;
    private final PayrollProducer payrollProducer;
    
    @Value("${payroll.csv.input-path}")
    private String inputPath;
    
    @Value("${payroll.csv.processed-path}")
    private String processedPath;
    
    @Scheduled(cron = "${payroll.scheduler.cron}")
    public void processCsvFiles() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  [PRODUCER] Starting scheduled CSV file processing          ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            Path inputDir = Paths.get(inputPath);
            Path processedDir = Paths.get(processedPath);
            
            // Create directories if they don't exist
            if (!Files.exists(inputDir)) {
                Files.createDirectories(inputDir);
                log.info("[PRODUCER] Created input directory: {}", inputDir);
            }
            
            if (!Files.exists(processedDir)) {
                Files.createDirectories(processedDir);
                log.info("[PRODUCER] Created processed directory: {}", processedDir);
            }
            
            // Process all CSV files in the input directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.csv")) {
                int fileCount = 0;
                int totalRecords = 0;
                
                for (Path file : stream) {
                    try {
                        log.info("[PRODUCER] 📂 Processing file: {}", file.getFileName());
                        
                        // Read CSV file
                        List<PayrollData> payrollDataList = csvReaderService.readCsvFile(file);
                        
                        // Send each record to Kafka
                        for (PayrollData payrollData : payrollDataList) {
                            payrollProducer.sendPayrollData(payrollData);
                            totalRecords++;
                        }
                        
                        // Move file to processed directory
                        csvReaderService.moveToProcessed(file, processedDir);
                        
                        fileCount++;
                        log.info("[PRODUCER] ✅ Successfully processed file: {} with {} records", 
                                file.getFileName(), payrollDataList.size());
                        
                    } catch (IOException | CsvException e) {
                        log.error("[PRODUCER] ❌ Error processing file: {}", file.getFileName(), e);
                    }
                }
                
                if (fileCount > 0) {
                    log.info("╔══════════════════════════════════════════════════════════════╗");
                    log.info("║  [PRODUCER] ✅ Completed: {} files, {} records sent         ║", fileCount, totalRecords);
                    log.info("╚══════════════════════════════════════════════════════════════╝");
                } else {
                    log.debug("[PRODUCER] No CSV files found in input directory");
                }
                
            } catch (IOException e) {
                log.error("[PRODUCER] ❌ Error reading input directory", e);
            }
            
        } catch (Exception e) {
            log.error("[PRODUCER] ❌ Unexpected error in scheduled task", e);
        }
    }
}
