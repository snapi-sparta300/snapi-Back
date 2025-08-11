// src/main/java/stparta300/snapi/domain/mission/dto/response/MissionImageResponse.java
package stparta300.snapi.domain.mission.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MissionImageResponse {
    private Long userMissionId;
    private Long tempImageId;
    private String imageUrl;
    private String sha256Hash;

    // 판정
    private String status;        // "PASS" | "FAIL"
    private Long awardedPoint;    // PASS 시 지급 포인트
    private Double confidence;    // PASS 시 AI 신뢰도
    private String classDetected; // PASS 시 탐지 클래스
    private String bbox;          // PASS 시 바운딩 박스
}