package com.team1.hrbank.entity;

import com.team1.hrbank.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeHistory extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private HistoryType type;

  @Column(name = "employee_number", nullable = false, length = 20)
  private String employeeNumber;

  @Column(name = "diff_json", columnDefinition = "TEXT")
  private String diffJson;

  @Column(length = 500)
  private String memo;

  @Column(name = "ip_address", nullable = false, length = 45)
  private String ipAddress;

  public static EmployeeHistory of(
      HistoryType type,
      String employeeNumber,
      String diffJson,
      String memo,
      String ipAddress
  ) {
    EmployeeHistory history = new EmployeeHistory();
    history.type = type;
    history.employeeNumber = employeeNumber;
    history.diffJson = diffJson;
    history.memo = memo;
    history.ipAddress = ipAddress;
    return history;
  }
}
