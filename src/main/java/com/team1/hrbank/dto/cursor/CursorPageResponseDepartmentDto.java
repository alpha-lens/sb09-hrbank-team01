package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.DepartmentDto;
import java.util.List;

public record CursorPageResponseDepartmentDto(
    List<DepartmentDto> content,
    String nextCursor,
    long nextIdAfter,
    long size,
    long totalElements,
    boolean hasNext
) {

}
