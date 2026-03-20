package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long>,
    JpaSpecificationExecutor<Department> {

  boolean existsByName(String name);

}