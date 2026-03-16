package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Department;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  boolean existsByName(String name);

  @Query("SELECT d FROM Department d " +
         "WHERE (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
         "AND (:idAfter IS NULL OR d.id > :idAfter) " +
         "ORDER BY d.id ASC")
  List<Department> findDepartmentsWithCursor(
      @Param("keyword") String keyword,
      @Param("idAfter") Long idAfter,
      Pageable pageable);

  @Query("SELECT COUNT(d) FROM Department d " +
      "WHERE (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  long countByKeyword(@Param("keyword") String keyword);

  @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Department> searchByKeyword(@Param("keyword") String keyword);

}