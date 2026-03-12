package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.ChangeLogDto;
import java.util.List;

public record CursorPageResponseChangeLogDto(
    List<ChangeLogDto> content,
    String nextCursor,
    long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
