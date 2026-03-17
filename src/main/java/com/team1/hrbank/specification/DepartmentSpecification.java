package com.team1.hrbank.specification;

import com.team1.hrbank.dto.request.DepartmentSearchRequest;
import com.team1.hrbank.entity.Department;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DepartmentSpecification {

  public static Specification<Department> filterBy(DepartmentSearchRequest request, Department cursorDept) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 1. 키워드 검색
      if (request.nameOrDescription() != null && !request.nameOrDescription().trim().isEmpty()) {
        String pattern = "%" + request.nameOrDescription().toLowerCase() + "%";
        Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
        Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
        predicates.add(cb.or(nameMatch, descMatch));
      }

      // 2. 복합 커서 로직
      if (cursorDept != null) {
        String sortField = request.sortField() != null ? request.sortField() : "id";
        boolean isAsc = !"desc".equalsIgnoreCase(request.sortDirection());

        if ("name".equals(sortField)) {
          // 이름 기준 정렬
          if (isAsc) {
            predicates.add(cb.or(
                cb.greaterThan(root.get("name"), cursorDept.getName()),
                cb.and(cb.equal(root.get("name"), cursorDept.getName()), cb.greaterThan(root.get("id"), cursorDept.getId()))
            ));
          } else {
            predicates.add(cb.or(
                cb.lessThan(root.get("name"), cursorDept.getName()),
                cb.and(cb.equal(root.get("name"), cursorDept.getName()), cb.greaterThan(root.get("id"), cursorDept.getId()))
            ));
          }
        } else if ("establishedDate".equals(sortField)) {
          // 설립일 기준 정렬
          if (isAsc) {
            predicates.add(cb.or(
                cb.greaterThan(root.get("establishedDate"), cursorDept.getEstablishedDate()),
                cb.and(cb.equal(root.get("establishedDate"), cursorDept.getEstablishedDate()), cb.greaterThan(root.get("id"), cursorDept.getId()))
            ));
          } else {
            predicates.add(cb.or(
                cb.lessThan(root.get("establishedDate"), cursorDept.getEstablishedDate()),
                cb.and(cb.equal(root.get("establishedDate"), cursorDept.getEstablishedDate()), cb.greaterThan(root.get("id"), cursorDept.getId()))
            ));
          }
        } else {
          // 기본 정렬 (ID)
          predicates.add(cb.greaterThan(root.get("id"), cursorDept.getId()));
        }
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}