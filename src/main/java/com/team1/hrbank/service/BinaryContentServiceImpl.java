package com.team1.hrbank.service;

import com.team1.hrbank.dto.BinaryContentDto;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.repository.BinaryContentRepository;
import com.team1.hrbank.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BinaryContentServiceImpl implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final FileStorageService fileStorageService;

    /**
     * 파일 저장 흐름:
     * 1. 메타데이터를 DB에 저장 → id 확보
     * 2. id를 파일명으로 디스크에 저장 → 실제 경로 획득
     * 3. filePath를 실제 경로로 업데이트 (JPA 더티 체킹으로 자동 반영)
     */
    @Override
    @Transactional
    public BinaryContent create(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        try {
            // 1단계: DB에 메타데이터 저장 (filePath는 임시 빈 문자열)
            BinaryContent entity = BinaryContent.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .filePath("")
                    .build();
            entity = binaryContentRepository.save(entity);

            // 2단계: id를 파일명으로 디스크에 저장
            String savedPath = fileStorageService.save(entity.getId(), file.getBytes());

            // 3단계: filePath 업데이트 (더티 체킹으로 트랜잭션 종료 시 UPDATE 쿼리 실행)
            entity.updateFilePath(savedPath);

            return entity;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getBytes(Long id) {
        binaryContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다. id=" + id));
        return fileStorageService.load(id);
    }

    @Override
    public BinaryContentDto getMetadata(Long id) {
        BinaryContent content = binaryContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다. id=" + id));
        return new BinaryContentDto(
                content.getId(),
                content.getFileName(),
                content.getSize(),
                content.getContentType()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        binaryContentRepository.findById(id).ifPresent(content -> {
            fileStorageService.delete(id);
            binaryContentRepository.delete(content);
        });
    }
}
