package com.team1.hrbank.specification;

import com.team1.hrbank.dto.request.EmployeeSearchRequest;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

public class EmployeeSpecification {

  public static Specification<Employee> filterBy(EmployeeSearchRequest request) {
    return (root, query, cb) -> {
      if (query.getResultType() != Long.class) {
        root.fetch("department", JoinType.LEFT);
      }

      List<Predicate> predicates = new ArrayList<>();

      // 1. 이름 또는 이메일 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.nameOrEmail())) {
        String pattern = "%" + request.nameOrEmail() + "%";
        predicates.add(cb.or(
            cb.like(root.get("name"), pattern),
            cb.like(root.get("email"), pattern)
        ));
      }

      // 2. 부서 이름 (부분 일치: LIKE %keyword%) - Join 필요
      if (StringUtils.hasText(request.departmentName())) {
        predicates.add(cb.like(root.join("department").get("name"), "%" + request.departmentName() + "%"));
      }

      // 3. 직함 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.position())) {
        predicates.add(cb.like(root.get("position"), "%" + request.position() + "%"));
      }

      // 4. 사원 번호 (부분 일치: LIKE %keyword%)
      if (StringUtils.hasText(request.employeeNumber())) {
        predicates.add(cb.like(root.get("employeeNumber"), "%" + request.employeeNumber() + "%"));
      }

      // 5. 입사일 (범위 조건: hireDateFrom <= hireDate <= hireDateTo)
      if (request.hireDateFrom() != null && request.hireDateTo() != null) {
        predicates.add(cb.between(root.get("hireDate"), request.hireDateFrom(), request.hireDateTo()));
      }

      // 6. 상태 (완전 일치)
      if (StringUtils.hasText(request.status())) {
        try {
          // String을 Enum으로 변환하여 완전 일치 비교
          predicates.add(cb.equal(root.get("status"), EmployeeStatus.valueOf(request.status())));
        } catch (IllegalArgumentException ignored) {
          // 잘못된 상태값이 들어올 경우 조건 무시 혹은 예외 처리
        }
      }

      // 모든 리스트의 조건을 AND로 결합
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}