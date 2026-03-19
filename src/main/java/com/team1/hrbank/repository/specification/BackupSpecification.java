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
        .and(cursorCondition(req.lastId()));
  }

  // worker 부분 일치
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

  // 커서 조건 (id 기준)
  private static Specification<Backup> cursorCondition(Long lastId) {
    return (root, query, cb) -> {
      if (lastId == null) return null;
      return cb.lessThan(root.get("id"), lastId);
    };
  }
}