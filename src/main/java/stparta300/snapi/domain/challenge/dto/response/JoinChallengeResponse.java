package stparta300.snapi.domain.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinChallengeResponse {
    private Long userId;           // User.id
    private String userName;       // User.userName
    private Long userChallengeId;  // UserChallenge.id
    private Long challengeId;      // Challenge.id
}