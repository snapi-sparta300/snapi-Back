package stparta300.snapi.domain.challenge.service;

import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.dto.response.CompleteChallengeResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;

public interface ChallengeService {
    ChallengeListResponse getChallenges();
    JoinChallengeResponse join(Long userId, Long challengeId);
    CompleteChallengeResponse complete(Long userId, Long challengeId);

}