package stparta300.snapi.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PointHistoryResponse {
    private Long userId;          // User.id
    private String userName;      // User.userName
    private Long currentPoint;    // User.userPoint
    private Long totalEarned;     // 완료 상태 합계
    private List<HistoryDto> histories;

    @Getter
    @Builder
    public static class HistoryDto {
        private Long userMissionId; // UserMission.id
        private Long challengeId;   // UserMission.challenge.id
        private Long missionId;     // UserMission.mission.id
        private String missionName; // Mission.name
        private Long point;         // Mission.point
        private String state;       // UserMission.state (예: PASS/IN_PROGRESS/FAIL)
        private String createdAt;   // BaseEntity.createdAt (ISO-8601)
    }
}