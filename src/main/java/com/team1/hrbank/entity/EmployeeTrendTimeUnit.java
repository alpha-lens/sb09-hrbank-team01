package com.team1.hrbank.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmployeeTrendTimeUnit {
  DAY, WEEK, MONTH, QUARTER, YEAR;

  @JsonCreator
  public static EmployeeTrendTimeUnit from(String s) {
    return EmployeeTrendTimeUnit.valueOf(s.toUpperCase());
  }
}
