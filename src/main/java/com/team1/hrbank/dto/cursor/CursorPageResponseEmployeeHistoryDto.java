package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.EmployeeHistoryDto;
import java.util.List;

public record CursorPageResponseEmployeeHistoryDto (
    //<T>?
    List<EmployeeHistoryDto> content,
    String nextCursor,
    //Long?
    long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
