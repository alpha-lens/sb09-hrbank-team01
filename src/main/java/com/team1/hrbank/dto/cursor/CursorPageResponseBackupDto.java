package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.BackupDto;
import java.util.List;

public record CursorPageResponseBackupDto(
    List<BackupDto> content,
    String nextCursor,
    long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
