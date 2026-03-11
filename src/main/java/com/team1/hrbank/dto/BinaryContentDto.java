package com.team1.hrbank.dto;

public record BinaryContentDto(
        Long id,
        String fileName,
        Long size,
        String contentType
) {}
