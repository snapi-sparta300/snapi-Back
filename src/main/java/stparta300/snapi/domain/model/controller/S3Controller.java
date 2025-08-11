package stparta300.snapi.domain.model.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stparta300.snapi.domain.model.service.S3UploadService;

import java.util.Map;

@Tag(name = "S3", description = "S3 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3UploadService s3UploadService;

    @Operation(summary = "이미지 업로드(서버 업로드 A안)",
            description = "파일을 서버로 전송하면 서버가 S3에 스트리밍 업로드 후 퍼블릭 URL을 반환합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", defaultValue = "uploads") String prefix
    ) throws Exception {

        String url = s3UploadService.upload(file, prefix);
        return ResponseEntity.ok(Map.of("url", url));
    }
}