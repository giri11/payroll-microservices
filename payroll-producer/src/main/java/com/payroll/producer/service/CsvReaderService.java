package com.payroll.producer.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.payroll.producer.dto.PayrollData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvReaderService {
    
    public List<PayrollData> readCsvFile(Path filePath) throws IOException, CsvException {
        List<PayrollData> payrollDataList = new ArrayList<>();
        
        log.info("[PRODUCER] 📄 Reading CSV file: {}", filePath.getFileName());
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> records = reader.readAll();
            
            // Skip header row (index 0)
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                
                try {
                    PayrollData payrollData = PayrollData.builder()
                            .employeeId(record[0].trim())
                            .firstName(record[1].trim())
                            .lastName(record[2].trim())
                            .email(record[3].trim())
                            .department(record[4].trim())
                            .position(record[5].trim())
                            .baseSalary(new BigDecimal(record[6].trim()))
                            .allowances(new BigDecimal(record[7].trim()))
                            .deductions(new BigDecimal(record[8].trim()))
                            .fileName(filePath.getFileName().toString())
                            .lineNumber(i + 1)
                            .build();
                    
                    // Calculate net salary
                    BigDecimal netSalary = payrollData.getBaseSalary()
                            .add(payrollData.getAllowances())
                            .subtract(payrollData.getDeductions());
                    payrollData.setNetSalary(netSalary);
                    
                    payrollDataList.add(payrollData);
                    
                    log.debug("[PRODUCER] Parsed payroll data for Employee ID: {} from line {}", 
                            payrollData.getEmployeeId(), i + 1);
                    
                } catch (Exception e) {
                    log.error("[PRODUCER] ❌ Error parsing CSV record at line {}: {}", i + 1, e.getMessage());
                }
            }
            
            log.info("[PRODUCER] ✅ Successfully read {} records from CSV file: {}", 
                    payrollDataList.size(), filePath.getFileName());
            
        } catch (IOException | CsvException e) {
            log.error("[PRODUCER] ❌ Error reading CSV file: {}", filePath.getFileName(), e);
            throw e;
        }
        
        return payrollDataList;
    }
    
    public void moveToProcessed(Path sourceFile, Path processedDir) throws IOException {
        if (!Files.exists(processedDir)) {
            Files.createDirectories(processedDir);
        }
        
        Path targetFile = processedDir.resolve(sourceFile.getFileName());
        Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("[PRODUCER] 📦 Moved file {} to processed directory", sourceFile.getFileName());
    }
}
