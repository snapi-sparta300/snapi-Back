package stparta300.snapi.domain.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteChallengeResponse {
    private Long userId;
    private String userName;
    private Long userChallengeId;
    private Long challengeId;

    private String state;         // "완료"
    private Long successMission;  // UserChallenge.successMission
    private Long totalMission;    // Challenge.totalMission
    private Long awardedPoint;    // Challenge.totalPoint
    private Long userPoint;       // 적립 후 User.userPoint
    private String completedAt;   // ISO-8601 (엔티티의 updatedAt/changedAt 기준)
}