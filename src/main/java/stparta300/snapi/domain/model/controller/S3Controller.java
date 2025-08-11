package stparta300.snapi.domain.model.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @Operation(
            summary = "이미지 업로드 (서버 업로드 A안)",
            description = "multipart/form-data로 파일을 전송하면 서버가 S3에 스트리밍 업로드 후 퍼블릭 URL을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UploadResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @Parameter(description = "업로드할 파일", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "S3 저장 경로 prefix (예: missions/123, users/{userId}/profile)", required = false, example = "uploads")
            @RequestParam(value = "prefix", defaultValue = "uploads") String prefix
    ) throws Exception {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "file이 비어 있습니다."));
        }

        String url = s3UploadService.upload(file, prefix);
        // 필요하면 key도 따로 내려줄 수 있습니다. (prefix/uuid_filename)
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Swagger용 응답 스키마(예시)
    static class UploadResponse {
        public String url;
    }
}