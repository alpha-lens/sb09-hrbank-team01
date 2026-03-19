package com.team1.hrbank.dto.dashboard;

// 회원 수 추이 정보

public record EmployeeTrendDto(
    String date,
    int count,
    int change,
    double changeRate
) {

}
