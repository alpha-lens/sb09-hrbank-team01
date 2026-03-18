package com.team1.hrbank.specification;

import com.team1.hrbank.dto.request.EmployeeSearchRequest;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

public class EmployeeSpecification {

  public static Specification<Employee> filterBy(EmployeeSearchRequest request) {
    return (root, query, cb) -> {
      // 1. 카운트 쿼리 여부 확인 (결과 타입이 Long인지 체크)
      boolean isCountQuery = query.getResultType() == Long.class || query.getResultType() == long.class;

      List<Predicate> predicates = new ArrayList<>();

      // 2. 이름 또는 이메일 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.nameOrEmail())) {
        String pattern = "%" + request.nameOrEmail() + "%";
        predicates.add(cb.or(
            cb.like(root.get("name"), pattern),
            cb.like(root.get("email"), pattern)
        ));
      }

      // 3. 부서 이름 (부분 일치: LIKE %keyword%) - Join 필요
      if (StringUtils.hasText(request.departmentName())) {
        predicates.add(cb.like(root.join("department", JoinType.LEFT).get("name"), "%" + request.departmentName() + "%"));
      }

      // 4. 직함 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.position())) {
        predicates.add(cb.like(root.get("position"), "%" + request.position() + "%"));
      }

      // 5. 사원 번호 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.employeeNumber())) {
        predicates.add(cb.like(root.get("employeeNumber"), "%" + request.employeeNumber() + "%"));
      }

      // 6. 입사일 (범위 조건: hireDateFrom <= hireDate <= hireDateTo)
      if (request.hireDateFrom() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), request.hireDateFrom()));
      }

      if (request.hireDateTo() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("hireDate"), request.hireDateTo()));
      }

      // 7. 상태 (완전 일치)
      if (StringUtils.hasText(request.status())) {
        try {
          // String을 Enum으로 변환하여 완전 일치 비교
          predicates.add(cb.equal(root.get("status"), EmployeeStatus.valueOf(request.status())));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid employee status: " + request.status());
        }
      }

      if (!isCountQuery) {
        root.fetch("department", JoinType.LEFT); // N+1 방지

        if (StringUtils.hasText(request.cursor())) {
          String sortField = StringUtils.hasText(request.sortField()) ? request.sortField() : "id";
          String cursorStr = request.cursor();

          // 요청된 정렬 방향 판별 (null 안전성 처리 포함)
          boolean isDesc = "DESC".equalsIgnoreCase(request.sortDirection());

          // '_'를 기준으로 값과 ID 분리 (id 단일 정렬일 경우 _가 없음)
          int lastDashIndex = cursorStr.lastIndexOf("_");

          if (!"id".equals(sortField) && lastDashIndex != -1) {
            String valueCursor = cursorStr.substring(0, lastDashIndex);
            Long idCursor = Long.parseLong(cursorStr.substring(lastDashIndex + 1));

            if ("hireDate".equals(sortField)) {
              // 날짜 타입
              LocalDate dateCursor = LocalDate.parse(valueCursor);
              Predicate compareValue = isDesc ? cb.lessThan(root.get(sortField), dateCursor) : cb.greaterThan(root.get(sortField), dateCursor);
              Predicate eqValue = cb.equal(root.get(sortField), dateCursor);
              Predicate compareId = isDesc ? cb.lessThan(root.get("id"), idCursor) : cb.greaterThan(root.get("id"), idCursor);

              predicates.add(cb.or(compareValue, cb.and(eqValue, compareId)));
            } else {
              // 일반 문자열 (name, position, employeeNumber 등)
              Predicate compareValue = isDesc ? cb.lessThan(root.get(sortField).as(String.class), valueCursor) : cb.greaterThan(root.get(sortField).as(String.class), valueCursor);
              Predicate eqValue = cb.equal(root.get(sortField).as(String.class), valueCursor);
              Predicate compareId = isDesc ? cb.lessThan(root.get("id"), idCursor) : cb.greaterThan(root.get("id"), idCursor);

              predicates.add(cb.or(compareValue, cb.and(eqValue, compareId)));
            }
          } else {
            // ID 기준 단일 커서
            Long idCursor = Long.parseLong(cursorStr);
            Predicate compareId = isDesc ? cb.lessThan(root.get("id"), idCursor) : cb.greaterThan(root.get("id"), idCursor);
            predicates.add(compareId);
          }
        }
      }

      // 모든 리스트의 조건을 AND로 결합
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<Employee> filterForCount(EmployeeSearchRequest request) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (StringUtils.hasText(request.nameOrEmail())) {
        String pattern = "%" + request.nameOrEmail() + "%";
        predicates.add(cb.or(
            cb.like(root.get("name"), pattern),
            cb.like(root.get("email"), pattern)
        ));
      }

      if (StringUtils.hasText(request.departmentName())) {
        predicates.add(cb.like(root.join("department").get("name"), "%" + request.departmentName() + "%"));
      }

      if (StringUtils.hasText(request.position())) {
        predicates.add(cb.like(root.get("position"), "%" + request.position() + "%"));
      }

      if (StringUtils.hasText(request.employeeNumber())) {
        predicates.add(cb.like(root.get("employeeNumber"), "%" + request.employeeNumber() + "%"));
      }

      if (request.hireDateFrom() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), request.hireDateFrom()));
      }

      if (request.hireDateTo() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("hireDate"), request.hireDateTo()));
      }

      if (StringUtils.hasText(request.status())) {
        try {
          predicates.add(cb.equal(root.get("status"), EmployeeStatus.valueOf(request.status())));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid employee status: " + request.status());
        }
      }

      // 커서 조건 제외 (totalElements 카운트용)
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}