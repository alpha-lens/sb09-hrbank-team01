package com.team1.hrbank.repository.specification;

import com.team1.hrbank.dto.request.BackupSearchRequest;
import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class BackupSpecification {

  // 전체 필터 조건 조합
  public static Specification<Backup> findByCondition(BackupSearchRequest req) {
    return Specification
        .where(workerContains(req.worker()))
        .and(startedAtFrom(req.startedAtFrom()))
        .and(startedAtTo(req.startedAtTo()))
        .and(statusEquals(req.status()))
        .and(cursorCondition(req.lastId(), req.sortField()));
  }

  private static Specification<Backup> workerContains(String worker) {
    return (root, query, cb) -> {
      if (!StringUtils.hasText(worker)) return null;
      return cb.like(cb.lower(root.get("worker")),
          "%" + worker.toLowerCase() + "%");
    };
  }

  // startedAt 범위 시작
  private static Specification<Backup> startedAtFrom(Instant from) {
    return (root, query, cb) -> {
      if (from == null) return null;
      return cb.greaterThanOrEqualTo(root.get("startedAt"), from);
    };
  }

  // startedAt 범위 끝
  private static Specification<Backup> startedAtTo(Instant to) {
    return (root, query, cb) -> {
      if (to == null) return null;
      return cb.lessThanOrEqualTo(root.get("startedAt"), to);
    };
  }

  // status 완전 일치
  private static Specification<Backup> statusEquals(BackupStatus status) {
    return (root, query, cb) -> {
      if (status == null) return null;
      return cb.equal(root.get("status"), status);
    };
  }

  // 커서 조건 (Keyset 페이지네이션)
  private static Specification<Backup> cursorCondition(Long lastId, String sortField) {
    return (root, query, cb) -> {
      if (lastId == null) return null;

      // lastId 기준으로 커서 백업 조회는 서비스에서 처리하므로
      // 여기선 id 기준으로만 처리
      return cb.lessThan(root.get("id"), lastId);
    };
  }
}
