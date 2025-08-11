package stparta300.snapi.domain.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChallengeDetailResponse {
    private Long challengeId;
    private String name;
    private String companyName;
    private String comment;
    private Long totalPoint;
    private Long maxCount;
    private Long currentCount;
    private Double progressRate;
    private Long totalMission;
    private List<MissionDto> missions;
    private UserStatusDto userStatus; // userId 제공 시만 채움

    @Getter
    @Builder
    public static class MissionDto {
        private Long missionId;
        private String name;
        private Long point;
        private String imageUrl;
        private boolean userJoined;
        private Long userMissionId;
        private String userState;
    }

    @Getter
    @Builder
    public static class UserStatusDto {
        private Long userChallengeId;
        private String state;
        private Long successMission;
        private Long totalMission;
        private Double progress;
    }
}