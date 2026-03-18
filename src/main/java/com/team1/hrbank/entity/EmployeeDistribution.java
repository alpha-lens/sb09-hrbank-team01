package com.team1.hrbank.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmployeeDistribution {
  DEPARTMENT, POSITION;

  @JsonCreator
  public static EmployeeStatus from(String s) {
    return EmployeeStatus.valueOf(s.toUpperCase());
  }
}
