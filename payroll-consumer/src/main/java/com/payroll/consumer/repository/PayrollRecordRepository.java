package com.payroll.consumer.repository;

import com.payroll.consumer.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByEmployeeId(String employeeId);
    List<PayrollRecord> findByPayrollDate(LocalDate payrollDate);
    List<PayrollRecord> findByStatus(String status);
    
    // For idempotent duplicate prevention
    java.util.Optional<PayrollRecord> findByEmployeeIdAndPayrollDate(String employeeId, LocalDate payrollDate);
}
