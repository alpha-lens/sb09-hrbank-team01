package com.team1.hrbank.dto.dashboard;

public record EmployeeDistributionDto(
    String groupKey,
    long count,
    double percentage
) {

}
