package com.payroll.consumer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_records",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_employee_payroll_date",
            columnNames = {"employeeId", "payrollDate"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String employeeId;
    
    @Column(nullable = false)
    private String employeeName;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal allowances;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal deductions;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal netSalary;
    
    @Column(nullable = false)
    private LocalDate payrollDate;
    
    @Column(nullable = false)
    private String status; // PENDING, PROCESSED, PAID
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
