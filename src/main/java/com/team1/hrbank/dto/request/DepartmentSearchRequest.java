package com.team1.hrbank.dto.request;

public record DepartmentSearchRequest(
    String keyword,
    Long cursor,
    Integer size
) {

}