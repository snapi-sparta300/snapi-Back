package stparta300.snapi.domain.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import java.net.URI;

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

    /** S3 key 를 직접 알고 있을 때 삭제 */
    public void deleteByKey(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(req);
    }

    /** 업로드 시 돌려준 퍼블릭 URL로 삭제 (virtual-hosted 형식 가정) */
    public void deleteByUrl(String url) {
        try {
            URI u = URI.create(url);
            // 예: https://{bucket}.s3.{region}.amazonaws.com/{key}
            String host = u.getHost();                   // {bucket}.s3.ap-northeast-2.amazonaws.com
            String path = u.getPath();                   // /challenges/1/missions/1/users/4/uuid_name.jpg
            String key  = path.startsWith("/") ? path.substring(1) : path;

            // 혹시라도 버킷이 다르면(안 맞으면) 방어적으로 그냥 key 기반 삭제
            if (host != null && host.startsWith(bucket + ".")) {
                deleteByKey(key);
            } else {
                // 업로드 시 생성한 URL 포맷이 다르면 여기서 파싱 규칙을 바꿔주세요.
                deleteByKey(key);
            }
        } catch (Exception ignored) {
            // 삭제 실패해도 비즈니스 플로우는 그대로 진행
        }
    }
}