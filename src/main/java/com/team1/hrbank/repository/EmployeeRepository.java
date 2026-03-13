package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeStatus;
import com.team1.hrbank.repository.projection.DistributionMapping;
import com.team1.hrbank.repository.projection.EmployeeTrendMapping;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  @Query("SELECT MAX(e.employeeNumber) FROM Employee e WHERE e.employeeNumber LIKE :prefix%")
  String findLastEmployeeNameByPrefix(@Param("prefix") String prefix);

  List<Employee> findAll();

  @Query(value = "SELECT COUNT(*) FROM employee WHERE status = :status "
                 + "AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
  long findEmployeeCount(@Param("status") EmployeeStatus status,
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query(value = """
      SELECT 
          to_char(created_at, 
              CASE 
                  WHEN :unit = 'DAY' THEN 'YYYY-MM-DD'
                  WHEN :unit = 'MONTH' THEN 'YYYY-MM'
                  WHEN :unit = 'YEAR' THEN 'YYYY'
                  ELSE 'YYYY-MM' 
              END
          ) as period,
          COUNT(*)::int as count
      FROM employees
      WHERE created_at BETWEEN :startDate AND :endDate
      GROUP BY period
      ORDER BY period ASC
      """, nativeQuery = true)
  List<EmployeeTrendMapping> getTrendData(@Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate, @Param("unit") String unit);

  @Query(value = """
          SELECT d.name as label, COUNT(e.id) as count 
          FROM employees e 
          JOIN departments d ON e.department_id = d.id
          WHERE e.status = :status
          GROUP BY d.name
          ORDER BY count DESC
      """, nativeQuery = true)
  List<DistributionMapping> findDistributionByDepartment(@Param("status") String status);

  // 직급별 분포
  @Query(value = """
          SELECT position as label, COUNT(*) as count 
          FROM employees 
          WHERE status = :status
          GROUP BY position
          ORDER BY count DESC
      """, nativeQuery = true)
  List<DistributionMapping> findDistributionByPosition(@Param("status") String status);

  boolean existsByUpdatedAtAfter(Instant lastBackupTime);

  @Query("SELECT e FROM Employee e JOIN FETCH e.department")
  Page<Employee> findAllWithDepartment(Pageable pageable);

  long countByDepartmentId(Long departmentId);

  boolean existsByDepartmentId(Long departmentId);

  boolean existsByEmail(String email);
}
