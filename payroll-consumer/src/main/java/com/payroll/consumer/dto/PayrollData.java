package com.payroll.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollData implements Serializable {
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private BigDecimal baseSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private String fileName;
    private Integer lineNumber;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
