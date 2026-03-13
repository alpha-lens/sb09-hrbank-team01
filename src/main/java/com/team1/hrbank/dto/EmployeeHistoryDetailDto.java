package com.team1.hrbank.dto;

import java.time.Instant;
import java.util.List;

public record EmployeeHistoryDetailDto(
    long id,
    String type,
    String employeeNumber,
    //오타수정 meno -> memo
    String memo,
    String ipAddress,
    //타입수정 Integer => Instant
    Instant at,
//  String employeeName,long profileImageId,
//  db테이블에서 diff_json으로 받으니 삭제
    List<DiffDto> diffs
) {

}
