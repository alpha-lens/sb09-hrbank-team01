package com.team1.hrbank.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmployeeStatus {
  ACTIVE, ON_LEAVE, RESIGNED;

  @JsonCreator
  public static EmployeeStatus from(String s) {
    return EmployeeStatus.valueOf(s.toUpperCase());
  }
}
