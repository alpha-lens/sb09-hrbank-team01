package com.team1.hrbank.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(
            @Value("${file.upload-directory:./file-data-map}") String location) {
        this.storageLocation = Paths.get(location).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다: " + location, e);
        }
    }

    /**
     * 파일 저장. 파일명은 fileId를 사용한다.
     *
     * @return 저장된 파일의 절대 경로 문자열
     */
    public String save(Long fileId, byte[] bytes) {
        try {
            Path target = storageLocation.resolve(String.valueOf(fileId));
            Files.write(target, bytes);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: id=" + fileId, e);
        }
    }

    /**
     * 파일 ID로 바이트 배열 반환.
     */
    public byte[] load(Long fileId) {
        try {
            Path file = storageLocation.resolve(String.valueOf(fileId));
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("파일을 읽을 수 없습니다: id=" + fileId, e);
        }
    }

    /**
     * 파일 삭제.
     */
    public void delete(Long fileId) {
        try {
            Path file = storageLocation.resolve(String.valueOf(fileId));
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: id=" + fileId, e);
        }
    }

    public boolean exists(Long fileId) {
        return Files.exists(storageLocation.resolve(String.valueOf(fileId)));
    }
}
