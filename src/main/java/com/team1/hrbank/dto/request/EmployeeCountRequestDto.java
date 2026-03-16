package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeCountRequestDto(EmployeeStatus status, LocalDate startDate,
                                      LocalDate endDate) {

}
