package stparta300.snapi.domain.mission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.challenge.repository.ChallengeRepository;
import stparta300.snapi.domain.mission.dto.response.MissionImageResponse;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.mission.entity.TempImage;
import stparta300.snapi.domain.mission.entity.VerifiedImage;
import stparta300.snapi.domain.mission.repository.MissionRepository;
import stparta300.snapi.domain.mission.repository.TempImageRepository;
import stparta300.snapi.domain.mission.repository.VerifiedImageRepository;
import stparta300.snapi.domain.model.service.S3UploadService;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.entity.UserMission;
import stparta300.snapi.domain.user.repository.UserChallengeRepository;
import stparta300.snapi.domain.user.repository.UserMissionRepository;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.model.enums.UserMissionState;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final TempImageRepository tempImageRepository;
    private final VerifiedImageRepository verifiedImageRepository;
    private final S3UploadService s3UploadService;

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ai.url:http://localhost:5000/api/verify}")
    private String aiUrl;

    @Override
    @Transactional
    public MissionImageResponse uploadMissionImage(Long challengeId, Long missionId, Long userId, MultipartFile file) {
        if (userId == null || file == null || file.isEmpty()) {
            throw new UserHandler(ErrorStatus.NULL_MEMBER_OR_FILE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.CHALLENGE_NOT_FOUND));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MISSION_NOT_FOUND));

        // 1) SHA-256
        String sha256 = sha256Hex(file);

        // ✅ (추가) 글로벌/사전 중복 검증: PASS만이 아니라 업로드 자체를 차단
        if (verifiedImageRepository.existsBySha256Hash(sha256)
                || tempImageRepository.existsBySha256Hash(sha256)) {
            // 프로젝트 에러코드에 맞춰 변경
            throw new UserHandler(ErrorStatus.IMAGE_ALREADY_EXISTS); // 409
        }

        // 2) 글로벌 중복
        if (verifiedImageRepository.existsBySha256Hash(sha256)) {
            throw new UserHandler(ErrorStatus.IMAGE_ALREADY_VERIFIED); // 409 매핑
        }

        // 3) UserMission upsert
        UserMission um = userMissionRepository.findByUser_IdAndMission_Id(userId, missionId)
                .orElseGet(() -> userMissionRepository.save(
                        UserMission.builder()
                                .user(user)
                                .challenge(challenge)
                                .mission(mission)
                                .state(UserMissionState.IN_PROGRESS)
                                .build()
                ));
        if (um.getState() == UserMissionState.PASS) {
            throw new UserHandler(ErrorStatus.MISSION_ALREADY_COMPLETED); // 409
        }

        // 4) S3 업로드
        String prefix = String.format("challenges/%d/missions/%d/users/%d", challengeId, missionId, userId);

        String imageUrl;
        try {
            imageUrl = s3UploadService.upload(file, prefix);
        } catch (Exception e) {  // ← S3UploadService 가 throws Exception 이면 반드시 처리해야 함
            // 로깅 원하면 여기서 로그
            throw new UserHandler(ErrorStatus.FILE_UPLOAD_FAIL); // 없으면 INTERNAL_SERVER_ERROR 로 대체
        }
        // 5) TempImage 1장 정책(업서트)
        List<TempImage> olds = tempImageRepository.findByUserMission_Id(um.getId());
        if (!olds.isEmpty()) tempImageRepository.deleteAllInBatch(olds);

        TempImage temp = tempImageRepository.save(
                TempImage.builder()
                        .userMission(um)
                        .missionNumber(1)
                        .imageUrl(imageUrl)
                        .sha256Hash(sha256)
                        .build()
        );

        // 6) AI 동기 호출
        AiResult ai = callAi(challengeId, missionId, userId, temp.getId(), imageUrl);

        // 7) PASS / FAIL
        if (ai.pass) {
            verifiedImageRepository.save(
                    VerifiedImage.builder()
                            .userMission(um)
                            .imageUrl(imageUrl)
                            .confidence(ai.confidence)
                            .classDetected(ai.classDetected)
                            .bbox(ai.bbox)
                            .sha256Hash(sha256)
                            .build()
            );
            um.setState(UserMissionState.PASS);

            // 포인트 적립
            long before = user.getUserPoint() == null ? 0L : user.getUserPoint();
            long award  = mission.getPoint()   == null ? 0L : mission.getPoint();
            user.setUserPoint(before + award);
            user.setUserPoint(user.getUserPoint() + mission.getPoint());

            userChallengeRepository.findByUser_IdAndChallenge_Id(userId, challengeId)
                    .ifPresent(uc -> uc.setSuccessMission(uc.getSuccessMission() + 1));

            return MissionImageResponse.builder()
                    .userMissionId(um.getId())
                    .tempImageId(temp.getId())
                    .imageUrl(imageUrl)
                    .sha256Hash(sha256)
                    .status("PASS")
                    .awardedPoint(mission.getPoint())
                    .confidence(ai.confidence)
                    .classDetected(ai.classDetected)
                    .bbox(ai.bbox)
                    .build();
        } else {
            um.setState(UserMissionState.FAIL);

            // 실패 시 Temp 이미지에서 삭제하도록 함
            tempImageRepository.delete(temp);

            return MissionImageResponse.builder()
                    .userMissionId(um.getId())
                    .tempImageId(temp.getId())
                    .imageUrl(imageUrl)
                    .sha256Hash(sha256)
                    .status("FAIL")
                    .build();
        }
    }

    /** checked exception을 모두 내부에서 처리해 컴파일 에러 제거 */
    private String sha256Hex(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                md.update(buf, 0, r);
            }
            byte[] dig = md.digest();
            StringBuilder sb = new StringBuilder(64);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new UserHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Rest/Jackson 예외를 모두 catch; multi-catch에서 부모/자식 중복 금지 */
    private AiResult callAi(Long challengeId, Long missionId, Long userId, Long tempImageId, String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = Map.of(
                    "challengeId", challengeId,
                    "missionId", missionId,
                    "userId", userId,
                    "tempImageId", tempImageId,
                    "imageUrl", imageUrl
            );
            ResponseEntity<String> res = restTemplate.exchange(
                    aiUrl, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class
            );
            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return AiResult.fail();

            JsonNode root;
            try {
                root = om.readTree(res.getBody());
            } catch (JsonProcessingException e) {
                return AiResult.fail();
            }

//            boolean success = root.path("success").asBoolean(false);
//            double confidence = root.path("confidence").asDouble(0.0);
//            String classDetected = root.path("classDetected").asText(null);
//            String bbox = root.path("bbox").asText(null);
//
//            boolean classOk = toTruth(classDetected);
//            boolean pass = success && confidence >= 0.9 && classOk;
//            return AiResult.pass(confidence, classDetected, bbox);
            boolean success = root.path("success").asBoolean(false);
            double confidence = root.path("confidence").asDouble(0.0);
            String classDetected = root.path("classDetected").asText(null);
            String bbox = root.path("bbox").asText(null);

            boolean classOk = toTruth(classDetected);
            boolean pass = success && confidence >= 0.9 && classOk;

            return pass ? AiResult.pass(confidence, classDetected, bbox)
                    : AiResult.fail();

        } catch (RestClientException e) {     // 이미 RuntimeException의 하위 → 단독 catch
            return AiResult.fail();
        } catch (RuntimeException e) {        // 기타 런타임
            return AiResult.fail();
        }
    }

//    private boolean toTruth(String v) {
//        if (v == null) return false;
//        String s = v.trim().toLowerCase();
//        return s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("palm");
//    }
    private boolean toTruth(String v) {
    if (v == null) return false;
    String s = v.trim().toLowerCase();
    return s.equals("true") || s.equals("1") || s.equals("yes");
}

    /** record → static class 로 교체(구 JDK 호환) */
    private static class AiResult {
        final boolean pass;
        final Double confidence;
        final String classDetected;
        final String bbox;

        private AiResult(boolean pass, Double confidence, String classDetected, String bbox) {
            this.pass = pass;
            this.confidence = confidence;
            this.classDetected = classDetected;
            this.bbox = bbox;
        }
        static AiResult pass(Double c, String cls, String b) { return new AiResult(true, c, cls, b); }
        static AiResult fail() { return new AiResult(false, null, null, null); }
    }
}