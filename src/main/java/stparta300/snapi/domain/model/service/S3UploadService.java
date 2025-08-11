package stparta300.snapi.domain.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 메모리에 올리지 않고 InputStream으로 바로 S3에 전송(스트리밍 업로드).
     * @param file   업로드 파일(Multipart)
     * @param prefix 저장 경로 prefix (예: "missions/123")
     * @return 퍼블릭 URL
     */
    public String upload(MultipartFile file, String prefix) throws Exception {
        String original = Objects.requireNonNull(file.getOriginalFilename());
        String safeName = original.replaceAll("\\s+", "_");
        String key = String.format("%s/%s_%s", trimSlash(prefix), UUID.randomUUID(), safeName);

        try (InputStream in = file.getInputStream()) {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(detectContentType(file))
                    .cacheControl("public, max-age=31536000")
                    .build();

            s3Client.putObject(req, RequestBody.fromInputStream(in, file.getSize()));
        }

        // 버킷이 퍼블릭 GetObject면 이 URL로 바로 접근 가능
        String regionId = s3Client.serviceClientConfiguration().region().id();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, regionId, key);
    }

    private String detectContentType(MultipartFile file) {
        String ct = file.getContentType();
        return (ct == null || ct.isBlank()) ? MediaType.APPLICATION_OCTET_STREAM_VALUE : ct;
    }

    private String trimSlash(String s) {
        if (s == null || s.isBlank()) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}