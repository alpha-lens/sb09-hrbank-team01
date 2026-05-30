package com.team1.hrbank.repository;

import com.team1.hrbank.entity.EmployeeHistory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, Long> {

  long countByCreatedAtBetween(Instant from, Instant to);

  @Query("SELECT MAX(h.id) FROM EmployeeHistory h")
  Optional<Long> findMaxId();

  // 다중 조건 조회 (네이티브 쿼리 - PostgreSQL null 파라미터 타입 추론 문제 해결)
  @Query(value = """
        SELECT * FROM employee_histories h
        WHERE (CAST(:employeeNumber AS VARCHAR) IS NULL OR CAST(:employeeNumber AS VARCHAR) = ''
                OR h.employee_number LIKE '%' || CAST(:employeeNumber AS VARCHAR) || '%')
          AND (CAST(:memo AS VARCHAR) IS NULL OR CAST(:memo AS VARCHAR) = ''
                OR h.memo LIKE '%' || CAST(:memo AS VARCHAR) || '%')
          AND (CAST(:ipAddress AS VARCHAR) IS NULL OR CAST(:ipAddress AS VARCHAR) = ''
                OR h.ip_address LIKE '%' || CAST(:ipAddress AS VARCHAR) || '%')
          AND (CAST(:type AS VARCHAR) IS NULL OR CAST(:type AS VARCHAR) = ''
                OR h.type = CAST(:type AS VARCHAR))
          AND (CAST(:atFrom AS TIMESTAMP) IS NULL
                OR h.created_at >= CAST(:atFrom AS TIMESTAMP))
          AND (CAST(:atTo AS TIMESTAMP) IS NULL
                OR h.created_at <= CAST(:atTo AS TIMESTAMP))
          AND (CAST(:idBefore AS BIGINT) = 0 OR h.id < CAST(:idBefore AS BIGINT))
        """, nativeQuery = true)
  List<EmployeeHistory> findHistoriesWithConditions(
      @Param("employeeNumber") String employeeNumber,
      @Param("memo") String memo,
      @Param("ipAddress") String ipAddress,
      @Param("type") String type,
      @Param("atFrom") Instant atFrom,
      @Param("atTo") Instant atTo,
      @Param("idBefore") Long idBefore,
      Pageable pageable
  );

  @Query(value = """
    SELECT COUNT(*) FROM employee_histories h
    WHERE (CAST(:employeeNumber AS VARCHAR) IS NULL OR CAST(:employeeNumber AS VARCHAR) = ''
            OR h.employee_number LIKE '%' || CAST(:employeeNumber AS VARCHAR) || '%')
      AND (CAST(:memo AS VARCHAR) IS NULL OR CAST(:memo AS VARCHAR) = ''
            OR h.memo LIKE '%' || CAST(:memo AS VARCHAR) || '%')
      AND (CAST(:ipAddress AS VARCHAR) IS NULL OR CAST(:ipAddress AS VARCHAR) = ''
            OR h.ip_address LIKE '%' || CAST(:ipAddress AS VARCHAR) || '%')
      AND (CAST(:type AS VARCHAR) IS NULL OR CAST(:type AS VARCHAR) = ''
            OR h.type = CAST(:type AS VARCHAR))
      AND (CAST(:atFrom AS TIMESTAMP) IS NULL
            OR h.created_at >= CAST(:atFrom AS TIMESTAMP))
      AND (CAST(:atTo AS TIMESTAMP) IS NULL
            OR h.created_at <= CAST(:atTo AS TIMESTAMP))
    """, nativeQuery = true)
    // idBefore는 카운트에서 제외
  long countByConditions(
      @Param("employeeNumber") String employeeNumber,
      @Param("memo") String memo,
      @Param("ipAddress") String ipAddress,
      @Param("type") String type,
      @Param("atFrom") Instant atFrom,
      @Param("atTo") Instant atTo
  );

}
