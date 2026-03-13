package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  boolean existsByName(String name);

}