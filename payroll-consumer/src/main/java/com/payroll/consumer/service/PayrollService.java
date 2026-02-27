package com.payroll.consumer.service;

import com.payroll.consumer.dto.PayrollData;
import com.payroll.consumer.model.Employee;
import com.payroll.consumer.model.PayrollRecord;
import com.payroll.consumer.repository.EmployeeRepository;
import com.payroll.consumer.repository.PayrollRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {
    
    private final EmployeeRepository employeeRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    
    @Transactional
    public void processPayrollData(PayrollData payrollData) {
        log.info("[CONSUMER] 💼 Processing payroll data for Employee ID: {}", payrollData.getEmployeeId());
        
        // Save or update employee
        Employee employee = saveOrUpdateEmployee(payrollData);
        
        // Create payroll record
        createPayrollRecord(payrollData);
        
        log.info("[CONSUMER] ✅ Completed processing payroll data for Employee ID: {}", payrollData.getEmployeeId());
    }
    
    private Employee saveOrUpdateEmployee(PayrollData payrollData) {
        Employee employee = employeeRepository.findByEmployeeId(payrollData.getEmployeeId())
                .orElse(new Employee());
        
        employee.setEmployeeId(payrollData.getEmployeeId());
        employee.setFirstName(payrollData.getFirstName());
        employee.setLastName(payrollData.getLastName());
        employee.setEmail(payrollData.getEmail());
        employee.setDepartment(payrollData.getDepartment());
        employee.setPosition(payrollData.getPosition());
        employee.setBaseSalary(payrollData.getBaseSalary());
        employee.setAllowances(payrollData.getAllowances());
        employee.setDeductions(payrollData.getDeductions());
        employee.setNetSalary(payrollData.getNetSalary());
        
        employee = employeeRepository.save(employee);
        log.info("[CONSUMER] 💾 Saved/Updated employee: {} {}", employee.getFirstName(), employee.getLastName());
        
        return employee;
    }
    
    private void createPayrollRecord(PayrollData payrollData) {
        LocalDate payrollDate = LocalDate.now();
        
        // Check if payroll record already exists for this employee and date (idempotent)
        PayrollRecord record = payrollRecordRepository
                .findByEmployeeIdAndPayrollDate(payrollData.getEmployeeId(), payrollDate)
                .orElse(PayrollRecord.builder()
                        .employeeId(payrollData.getEmployeeId())
                        .employeeName(payrollData.getFullName())
                        .payrollDate(payrollDate)
                        .build());
        
        // Update fields (idempotent - can be run multiple times safely)
        record.setDepartment(payrollData.getDepartment());
        record.setBaseSalary(payrollData.getBaseSalary());
        record.setAllowances(payrollData.getAllowances());
        record.setDeductions(payrollData.getDeductions());
        record.setNetSalary(payrollData.getNetSalary());
        record.setStatus("PROCESSED");
        
        payrollRecordRepository.save(record);
        log.info("[CONSUMER] 📝 Created/Updated payroll record for Employee ID: {} with net salary: {}", 
                payrollData.getEmployeeId(), payrollData.getNetSalary());
    }
}
