package com.team1.hrbank.controller;

import com.team1.hrbank.dto.BinaryContentDto;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.service.BinaryContentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BinaryContentDto> upload(@RequestParam("file") MultipartFile file) {
        BinaryContent created = binaryContentService.create(file);
        BinaryContentDto dto = new BinaryContentDto(
                created.getId(),
                created.getFileName(),
                created.getSize(),
                created.getContentType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BinaryContentDto> getMetadata(@PathVariable Long id) {
        BinaryContentDto metadata = binaryContentService.getMetadata(id);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        BinaryContentDto metadata = binaryContentService.getMetadata(id);
        byte[] bytes = binaryContentService.getBytes(id);

        String fileName = Normalizer.normalize(metadata.fileName(), Normalizer.Form.NFC);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.contentType()));
        headers.setContentLength(bytes.length);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        binaryContentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
