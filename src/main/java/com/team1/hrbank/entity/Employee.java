package com.team1.hrbank.entity;

import com.team1.hrbank.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.H2DurationIntervalSecondJdbcType;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseUpdatableEntity {
  @JdbcType(H2DurationIntervalSecondJdbcType.class)
  @Column(length = 20, nullable = false)
  private String employeeNumber;
  @Column(length = 50, nullable = false)
  private String name;
  @Column(length = 100, nullable = false)
  private String email;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id", nullable = false)
  private Department departmentId;
  @Column(length = 50, name = "position", nullable = false)
  private String position;
  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "profile_image_id")
  private ProfileImage profileImageId;

  public Employee(String employeeNumber, String name, String email, Department departmentId, String position, LocalDate hireDate, Status status, ProfileImage profileImageId) {
    
  }
}
