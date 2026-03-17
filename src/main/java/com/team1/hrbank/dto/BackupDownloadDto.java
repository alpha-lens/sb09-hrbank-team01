package com.team1.hrbank.dto;

public record BackupDownloadDto(
    String fileName,
    String contentType,
    String filePath
) {

}
