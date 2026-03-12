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

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private LocalDate establishedDate;

  private Department(String name, String description, LocalDate establishedDate) {
    this.name = name;
    this.description = description;
    this.establishedDate = establishedDate;
  }

  public static Department create(String name, String description, LocalDate establishedDate) {
    return new Department(name, description, establishedDate);
  }

  public boolean update(String newName, String newDescription, LocalDate newEstablishedDate) {
    boolean isUpdated = false;

    if (newName != null && !newName.equals(this.name)) {
      this.name = newName;
      isUpdated = true;
    }
    if (newDescription != null && !newDescription.equals(this.description)) {
      this.description = newDescription;
      isUpdated = true;
    }
    if (newEstablishedDate != null && !newEstablishedDate.equals(this.establishedDate)) {
      this.establishedDate = newEstablishedDate;
      isUpdated = true;
    }

    return isUpdated;
  }
}