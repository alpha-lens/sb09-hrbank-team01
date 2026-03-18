package com.team1.hrbank.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmployeeDistribution {
  DEPARTMENT, POSITION;

  @JsonCreator
  public static EmployeeDistribution from(String s) {
    return EmployeeDistribution.valueOf(s.toUpperCase());
  }
}
