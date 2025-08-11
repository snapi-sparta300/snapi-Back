package stparta300.snapi.domain.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChallengeListResponse {
    private List<Item> challenges;

    @Getter
    @Builder
    public static class Item {
        private Long challengeId;   // Challenge.id
        private String name;        // Challenge.name
        private String companyName; // Challenge.companyName
        private String comment;     // Challenge.comment
        private Long totalMission;  // Challenge.totalMission
        private Long maxCount;      // Challenge.maxCount
        private Long currentCount;  // Challenge.count
        private Long totalPoint;    // Challenge.totalPoint
    }
}