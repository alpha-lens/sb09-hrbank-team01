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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Getter
@Builder(builderMethodName = "builder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Employee extends BaseUpdatableEntity {
  @Column(length = 20, nullable = false, unique = true)
  private String employeeNumber;
  @Column(length = 50, nullable = false)
  private String name;
  @Column(length = 100, nullable = false, unique = true)
  private String email;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;
  @Column(length = 50, name = "position", nullable = false)
  private String position;
  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private EmployeeStatus status;
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "profile_image_id")
  private BinaryContent profileImage;

  public static Employee of(String employeeNumber, String name, String email, Department department, String position) {
    return Employee.builder()
        .employeeNumber(employeeNumber)
        .name(name)
        .email(email)
        .department(department)
        .position(position)
        .hireDate(LocalDate.now())
        .status(EmployeeStatus.ACTIVE)
        .build();
  }

  public void update(String name, String email, Department department, String position, LocalDate hireDate, EmployeeStatus status) {
    this.name = name;
    this.email = email;
    this.department = department;
    this.position = position;
    this.hireDate = hireDate;
    this.status = status;
  }

  public void updateProfileImage(BinaryContent profileImage) {
    this.profileImage = profileImage;
  }
}
