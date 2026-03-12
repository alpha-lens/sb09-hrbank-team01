package com.team1.hrbank.entity;

import com.team1.hrbank.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseUpdatableEntity {

  @Column(length = 100, nullable = false,  unique = true)
  private String name;

  @Column(length = 500, nullable = false)
  private String description;

  @Column(nullable = false)
  private LocalDate establishedDate;

  public Department(String name, String description, LocalDate establishedDate) {
    this.name = name;
    this.description = description;
    this.establishedDate = establishedDate;
  }

  public void update(String newName, String newDescription,  LocalDate newEstablishedDate) {
    if (newName != null && newName.equals(this.name)) {
      this.name = newName;
    }
    if (newDescription != null && newDescription.equals(this.description)) {
      this.description = newDescription;
    }
    if (newEstablishedDate != null && !newEstablishedDate.equals(this.establishedDate)) {
      this.establishedDate = newEstablishedDate;
    }
  }
}