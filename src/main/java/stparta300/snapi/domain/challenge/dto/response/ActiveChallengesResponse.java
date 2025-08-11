package stparta300.snapi.domain.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActiveChallengesResponse {
    private Long userId;
    private String userName;
    private List<Item> challenges;

    @Getter
    @Builder
    public static class Item {
        private Long userChallengeId;
        private Long challengeId;
        private String name;
        private String companyName;
        private String comment;
        private Long totalMission;
        private Long successMission;
        private Double progress;       // success / total
        private Long maxCount;
        private Long currentCount;
        private Long totalPoint;
        private String state;          // 고정: "진행중"
    }
}