package com.team1.hrbank.entity;

import com.team1.hrbank.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BinaryContent extends BaseEntity {

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    // 디스크 저장 경로 (ex: ./file-data-map/1)
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // DB 저장 후 id 기반 경로 확정 시 호출
    public void updateFilePath(String filePath) {
        this.filePath = filePath;
    }
}
