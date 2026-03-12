package com.team1.hrbank.dto;

public record BinaryContentDto(
    long id,
    String fileName,
    long size,
    String contentType
) {

}
