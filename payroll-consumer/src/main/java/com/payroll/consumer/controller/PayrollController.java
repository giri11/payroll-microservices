package com.payroll.consumer.controller;

import com.payroll.consumer.model.Employee;
import com.payroll.consumer.model.PayrollRecord;
import com.payroll.consumer.repository.EmployeeRepository;
import com.payroll.consumer.repository.PayrollRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PayrollController {
    
    private final EmployeeRepository employeeRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("[CONSUMER] Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Payroll Consumer");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("[CONSUMER] Fetching all employees");
        List<Employee> employees = employeeRepository.findAll();
        log.info("[CONSUMER] Found {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String employeeId) {
        log.info("[CONSUMER] Fetching employee with ID: {}", employeeId);
        return employeeRepository.findByEmployeeId(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/payroll-records")
    public ResponseEntity<List<PayrollRecord>> getAllPayrollRecords() {
        log.info("[CONSUMER] Fetching all payroll records");
        List<PayrollRecord> records = payrollRecordRepository.findAll();
        log.info("[CONSUMER] Found {} payroll records", records.size());
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payroll-records/employee/{employeeId}")
    public ResponseEntity<List<PayrollRecord>> getPayrollRecordsByEmployee(
            @PathVariable String employeeId) {
        log.info("[CONSUMER] Fetching payroll records for employee: {}", employeeId);
        List<PayrollRecord> records = payrollRecordRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payroll-records/date/{date}")
    public ResponseEntity<List<PayrollRecord>> getPayrollRecordsByDate(
            @PathVariable String date) {
        log.info("[CONSUMER] Fetching payroll records for date: {}", date);
        LocalDate payrollDate = LocalDate.parse(date);
        List<PayrollRecord> records = payrollRecordRepository.findByPayrollDate(payrollDate);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payroll-records/today")
    public ResponseEntity<List<PayrollRecord>> getTodayPayrollRecords() {
        log.info("[CONSUMER] Fetching today's payroll records");
        LocalDate today = LocalDate.now();
        List<PayrollRecord> records = payrollRecordRepository.findByPayrollDate(today);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payroll-records/search")
    public ResponseEntity<Map<String, Object>> searchPayrollRecords(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String date) {
        log.info("[CONSUMER] Searching payroll records - employeeId: {}, date: {}", employeeId, date);
        
        List<PayrollRecord> records;
        
        if (employeeId != null && date != null) {
            // Search by both employee and date
            LocalDate payrollDate = LocalDate.parse(date);
            records = payrollRecordRepository.findByEmployeeIdAndPayrollDate(employeeId, payrollDate)
                    .map(List::of)
                    .orElse(List.of());
        } else if (employeeId != null) {
            // Search by employee only
            records = payrollRecordRepository.findByEmployeeId(employeeId);
        } else if (date != null) {
            // Search by date only
            LocalDate payrollDate = LocalDate.parse(date);
            records = payrollRecordRepository.findByPayrollDate(payrollDate);
        } else {
            // No filter, return all
            records = payrollRecordRepository.findAll();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalFound", records.size());
        result.put("records", records);
        result.put("filters", Map.of(
            "employeeId", employeeId != null ? employeeId : "all",
            "date", date != null ? date : "all"
        ));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/payroll-records/duplicates")
    public ResponseEntity<Map<String, Object>> checkDuplicates() {
        log.info("[CONSUMER] Checking for duplicate payroll records");
        
        List<PayrollRecord> allRecords = payrollRecordRepository.findAll();
        
        // Group by employeeId + payrollDate to find duplicates
        Map<String, List<PayrollRecord>> grouped = allRecords.stream()
            .collect(Collectors.groupingBy(
                record -> record.getEmployeeId() + "_" + record.getPayrollDate()
            ));
        
        // Filter only groups with > 1 record (duplicates)
        Map<String, List<PayrollRecord>> duplicates = grouped.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRecords", allRecords.size());
        result.put("duplicateGroups", duplicates.size());
        result.put("hasDuplicates", !duplicates.isEmpty());
        result.put("duplicates", duplicates);
        
        log.info("[CONSUMER] Duplicate check: {} total records, {} duplicate groups", 
                allRecords.size(), duplicates.size());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[CONSUMER] Fetching system statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("totalPayrollRecords", payrollRecordRepository.count());
        
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Long> departmentCount = new HashMap<>();
        
        for (Employee emp : employees) {
            departmentCount.merge(emp.getDepartment(), 1L, Long::sum);
        }
        
        stats.put("employeesByDepartment", departmentCount);
        
        log.info("[CONSUMER] Statistics: {}", stats);
        return ResponseEntity.ok(stats);
    }
}
