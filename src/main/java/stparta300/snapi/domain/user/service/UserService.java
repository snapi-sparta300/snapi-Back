package stparta300.snapi.domain.user.service;

import stparta300.snapi.domain.challenge.dto.response.ActiveChallengesResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.dto.request.UpdateMemberRequest;
import stparta300.snapi.domain.user.dto.response.*;

public interface UserService {
    Long login(LoginRequest request); // 성공 시 userId 반환
    SignupResponse signup(SignupRequest request);
    ProfileSetupResponse setupProfile(Long userId, ProfileSetupRequest request);
    UserDetailResponse getMemberProfile(Long userId);
    UpdateMemberResponse updateMember(Long userId, UpdateMemberRequest request);
    PointHistoryResponse getPointHistory(Long userId);
    ActiveChallengesResponse getActiveChallenges(Long userId);

}