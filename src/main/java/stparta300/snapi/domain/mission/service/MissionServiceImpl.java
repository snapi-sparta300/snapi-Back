package stparta300.snapi.domain.mission.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
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
import stparta300.snapi.domain.model.enums.UserMissionState;
import stparta300.snapi.domain.model.service.S3UploadService;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserMission;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.user.repository.UserChallengeRepository;
import stparta300.snapi.domain.user.repository.UserMissionRepository;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;

import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
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

    /** Spring 6+ RestClient (RestClientConfig에서 Bean 제공) */
    private final RestClient aiRestClient;

    /** 통과 임계값은 yml에서 조정 가능 (기본 0.6) */
    @Value("${app.ai.pass-threshold:0.6}")
    private double passThreshold;

    @Override
    @Transactional
    public MissionImageResponse uploadMissionImage(Long challengeId, Long missionId, Long userId, MultipartFile file) {
        if (userId == null || file == null || file.isEmpty()) {
            throw new UserHandler(ErrorStatus.NULL_MEMBER_OR_FILE);
        }

        // 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.CHALLENGE_NOT_FOUND));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MISSION_NOT_FOUND));

        // 1) SHA-256 계산 (업로드 전)
        String sha256 = sha256Hex(file);

        // 2) Temp 테이블 중복 검사(요구사항: Temp만 검사)
        if (tempImageRepository.existsBySha256Hash(sha256)) {
            throw new UserHandler(ErrorStatus.IMAGE_ALREADY_EXISTS); // 409
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
        final String imageUrl;
        try {
            imageUrl = s3UploadService.upload(file, prefix);
        } catch (Exception e) {
            throw new UserHandler(ErrorStatus.FILE_UPLOAD_FAIL);
        }

        // 5) TempImage “1장 정책” → 기존 것 삭제 후 새로 저장
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

        // 7) PASS / FAIL 분기 처리
        if (ai.pass) {
            // PASS → Verified 저장, UserMission 완료, 포인트 적립, UserChallenge 성공 카운트 +1
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

            long curr = Optional.ofNullable(user.getUserPoint()).orElse(0L);
            long award = Optional.ofNullable(mission.getPoint()).orElse(0L);
            user.setUserPoint(curr + award);

            userChallengeRepository.findByUser_IdAndChallenge_Id(userId, challengeId)
                    .ifPresent(uc -> uc.setSuccessMission(uc.getSuccessMission() + 1));

            return MissionImageResponse.builder()
                    .userMissionId(um.getId())
                    .tempImageId(temp.getId())
                    .imageUrl(imageUrl)
                    .sha256Hash(sha256)
                    .status("PASS")
                    .awardedPoint(award)
                    .confidence(ai.confidence)
                    .classDetected(ai.classDetected)
                    .bbox(ai.bbox)
                    .build();
        } else {
            // FAIL → UserMission 실패, Temp 삭제, S3 객체 삭제(요구사항 유지)
            um.setState(UserMissionState.FAIL);
            tempImageRepository.delete(temp);
            try {
                s3UploadService.deleteByUrl(imageUrl);
            } catch (Exception ignore) { /* 삭제 실패해도 흐름 유지 */ }

            return MissionImageResponse.builder()
                    .userMissionId(um.getId())
                    .tempImageId(temp.getId())
                    .imageUrl(imageUrl)
                    .sha256Hash(sha256)
                    .status("FAIL")
                    .build();
        }
    }

    /** 파일의 SHA-256 해시(소문자 hex) */
    private String sha256Hex(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                md.update(buf, 0, r);
            }
            byte[] dig = md.digest();
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UserHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new UserHandler(ErrorStatus.FILE_UPLOAD_FAIL);
        }
    }

    /** RestClient로 AI 서버에 JSON POST → 응답 해석(키/타입 변동에 내성 있게) */
    private AiResult callAi(Long challengeId, Long missionId, Long userId, Long tempImageId, String imageUrl) {
        try {
            Map<String, Object> payload = Map.of(
                    "challengeId", challengeId,
                    "missionId",   missionId,
                    "userId",      userId,
                    "tempImageId", tempImageId,
                    "imageUrl",    imageUrl
            );

            ResponseEntity<Map> res = aiRestClient
                    .post()
                    .uri(URI.create("/predict"))
                    .body(payload)
                    .retrieve()
                    .toEntity(Map.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                log.warn("AI response not 2xx or empty: {}", res.getStatusCode());
                return AiResult.fail();
            }

            Map<String, Object> body = res.getBody();

            // success: boolean 기대
            boolean success = asBoolean(body.get("success"));

            // confidence: "confidence" 또는 "Confidence"
            Double confidence = asDouble(
                    body.containsKey("confidence") ? body.get("confidence") : body.get("Confidence")
            );

            // classDetected: boolean 또는 문자열("True"/"False"/클래스명)
            Object rawCls = body.get("classDetected");
            String classDetected = normalizeClassDetected(rawCls); // "true"/"false"/"palm" 등 문자열화

            // bbox: 문자열 또는 배열(List/Array)
            Object rawBbox = body.get("bbox");
            String bbox = normalizeBbox(rawBbox); // "x1,y1,x2,y2" 형태로 정규화(없으면 null)

            boolean classOk = toTruth(classDetected);
            double conf = confidence == null ? 0.0 : confidence;

            boolean pass = success && classOk && conf >= passThreshold;

            if (!pass) {
                log.info("AI FAIL => success={}, classOk={}, confidence={}, threshold={}, classDetected={}, bbox={}",
                        success, classOk, conf, passThreshold, classDetected, bbox);
            } else {
                log.info("AI PASS  => success={}, classOk={}, confidence={}, threshold={}, classDetected={}, bbox={}",
                        success, classOk, conf, passThreshold, classDetected, bbox);
            }

            return pass ? AiResult.pass(conf, classDetected, bbox) : AiResult.fail();

        } catch (Exception e) {
            // 서버 미가동/타임아웃/파싱 실패 등은 FAIL로 처리(정책상)
            log.warn("AI call failed, treat as FAIL. reason={}", e.toString());
            return AiResult.fail();
        }
    }

    /** "true"/"1"/"yes"(대소문자 무시) → true */
    private boolean toTruth(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("true") || s.equals("1") || s.equals("yes");
    }

    /** 다양한 타입(Boolean/Number/String)을 Double로 안전 변환 */
    private Double asDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        if (o instanceof String s) {
            try { return Double.parseDouble(s.trim()); } catch (Exception ignored) {}
        }
        return null;
    }

    /** 다양한 타입(Boolean/String/Number)을 boolean으로 안전 변환 */
    private boolean asBoolean(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        if (o instanceof String s) return toTruth(s);
        return false;
    }

    /** classDetected의 원시값을 문자열로 정규화 */
    private String normalizeClassDetected(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Boolean b) return b ? "true" : "false";
        return String.valueOf(raw);
    }

    /** bbox가 배열이면 "x1,y1,x2,y2"로 직렬화, 문자열이면 그대로, 아니면 null */
    @SuppressWarnings("unchecked")
    private String normalizeBbox(Object raw) {
        if (raw == null) return null;
        if (raw instanceof String s) return s.isBlank() ? null : s;
        if (raw instanceof List<?> list && !list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Object v : list) {
                if (sb.length() > 0) sb.append(',');
                sb.append(v);
            }
            return sb.toString();
        }
        if (raw.getClass().isArray()) {
            Object[] arr = (Object[]) raw;
            return String.join(",", Arrays.stream(arr).map(String::valueOf).toList());
        }
        return null;
    }

    /** 내부 전달용 결과 포맷 */
    private static class AiResult {
        final boolean pass;
        final Double  confidence;
        final String  classDetected;
        final String  bbox;

        private AiResult(boolean pass, Double confidence, String classDetected, String bbox) {
            this.pass = pass;
            this.confidence = confidence;
            this.classDetected = classDetected;
            this.bbox = bbox;
        }
        static AiResult pass(Double c, String cls, String b) { return new AiResult(true,  c, cls, b); }
        static AiResult fail()                                 { return new AiResult(false, null, null, null); }
    }
}