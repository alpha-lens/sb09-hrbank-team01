package com.team1.hrbank.dto;

import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {
  public static <T> CursorPageResponse<T> of(
      List<T> allResults,
      int size,
      long totalElements,
      Function<T, Long> idExtractor
  ) {
    boolean hasNext = allResults.size() > size;
    List<T> content = hasNext ? allResults.subList(0, size) : allResults;

    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !content.isEmpty()) {
      T last = content.get(content.size() - 1);
      nextIdAfter = idExtractor.apply(last);
      nextCursor = String.valueOf(nextIdAfter);
    }

    return new CursorPageResponse<>(
        content, nextCursor, nextIdAfter, size, totalElements, hasNext
    );
  }
}
