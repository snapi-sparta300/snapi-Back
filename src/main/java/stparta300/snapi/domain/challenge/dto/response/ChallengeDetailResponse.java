package stparta300.snapi.domain.challenge.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChallengeDetailResponse {
    private Long challengeId;
    private String name;
    private String companyName;
    private String comment;
    private Long totalPoint;
    private Long maxCount;
    private Long currentCount;
    private Double progressRate; // 전체 달성률(%)
    private Long totalMission;
    private List<MissionDto> missions;
    private UserStatusDto userStatus; // userId 없으면 null(-> 미포함)

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MissionDto {
        private Long missionId;
        private String name;
        private Long point;
        private String imageUrl;     // 없으면 null(-> 미포함)
        private boolean userJoined;  // userId 없으면 항상 false
        private Long userMissionId;  // 참여 안했으면 null(-> 미포함)
        private String userState;    // 참여 안했으면 null(-> 미포함)
    }

    @Getter
    @Builder
    public static class UserStatusDto {
        private Long userChallengeId;
        private String state;
        private Long successMission;
        private Long totalMission;
        private Double progress; // 0~1
    }
}