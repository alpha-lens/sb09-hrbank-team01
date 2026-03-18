package com.team1.hrbank.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Status {
  ACTIVE,
  ON_LEAVE,
  RESIGNED;

  @JsonCreator
  public static Status from(String s) {
    return Status.valueOf(s.toUpperCase());
  }
}
