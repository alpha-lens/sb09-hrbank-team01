package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Department;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long>,
    JpaSpecificationExecutor<Department> {

  boolean existsByName(String name);

}