package com.team1.hrbank.dto.request;

public record DepartmentSearchRequest(
    String nameOrDescription,
    Long cursor,
    Integer size
) {

}