package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.EmployeeDistribution;
import com.team1.hrbank.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeDistributionRequestDto(LocalDate startDate, LocalDate endDate,
                                             EmployeeDistribution distribution,
                                             EmployeeStatus status) {

}
