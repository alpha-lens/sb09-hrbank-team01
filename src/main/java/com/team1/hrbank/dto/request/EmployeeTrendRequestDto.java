package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.EmployeeTrendTimeUnit;
import java.time.LocalDate;

public record EmployeeTrendRequestDto(LocalDate startDate, LocalDate endDate,
                                      EmployeeTrendTimeUnit unit) {

}
