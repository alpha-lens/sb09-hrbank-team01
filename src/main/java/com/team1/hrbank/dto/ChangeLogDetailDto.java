package com.team1.hrbank.dto;

import java.util.List;

public record ChangeLogDetailDto(
    long id,
    String type,
    String employeeNumber,
    String meno,
    String ipAddress,
    Integer at,
    String employeeName,
    long profileImageId,
    List<DiffDto> diffs
) {

}
