package com.team1.hrbank.service;

import com.team1.hrbank.dto.BinaryContentDto;
import com.team1.hrbank.entity.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

public interface BinaryContentService {

    /**
     * MultipartFile을 받아 메타데이터를 DB에, 실제 파일을 디스크에 저장한다.
     *
     * @return 저장된 BinaryContent 엔티티
     */
    BinaryContent create(MultipartFile file);

    /**
     * 파일 ID로 바이트 배열을 반환한다. (다운로드용)
     */
    byte[] getBytes(Long id);

    /**
     * 파일 메타데이터를 DTO로 반환한다.
     */
    BinaryContentDto getMetadata(Long id);

    /**
     * 파일을 삭제한다. (DB 레코드 + 디스크 파일)
     */
    void delete(Long id);
}
