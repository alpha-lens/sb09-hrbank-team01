package com.team1.hrbank.repository;

import com.team1.hrbank.entity.EmployeeHistory;
import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, Long> {

  List<EmployeeHistory> findByIdBetween(Long from, Long to);

//  // 다중 조건 조회 (JPQL로 직접 작성)
//  @Query("""
//        SELECT h
//        FROM EmployeeHistory h
//        WHERE (:employeeNumber IS NULL
//                OR h.employeeNumber LIKE %:employeeNumber%)
//          AND (:memo IS NULL
//                OR h.memo LIKE %:memo%)
//          AND (:ipAddress IS NULL
//                OR h.ipAddress LIKE %:ipAddress%)
//          AND (:type IS NULL
//                OR h.type = :type)
//          AND (:atFrom IS NULL
//                OR h.createdAt >= :atFrom)
//          AND (:atTo IS NULL
//                OR h.createdAt <= :atTo)
//          AND (:idAfter IS NULL
//                OR h.id < :idAfter)
//        ORDER BY h.createdAt DESC
//        """)
//  List<EmployeeHistory> findHistoriesWithConditions(
//      @Param("employeeNumber") String employeeNumber,
//      @Param("memo") String memo,
//      @Param("ipAddress") String ipAddress,
//      @Param("type") HistoryType type,
//      @Param("atFrom") Instant atFrom,
//      @Param("atTo") Instant atTo,
//      @Param("idAfter") Long idAfter
//  );

}
