package com.team1.hrbank.dto;

public record BackupDto(
    long id,
    String worker,
    String startedAt,
    String endedAt,
    String status,
    Long fileId
) {

}
