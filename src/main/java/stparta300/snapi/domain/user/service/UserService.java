package stparta300.snapi.domain.user.service;

import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.dto.request.UpdateMemberRequest;
import stparta300.snapi.domain.user.dto.response.ProfileSetupResponse;
import stparta300.snapi.domain.user.dto.response.SignupResponse;
import stparta300.snapi.domain.user.dto.response.UpdateMemberResponse;
import stparta300.snapi.domain.user.dto.response.UserDetailResponse;

public interface UserService {
    Long login(LoginRequest request); // 성공 시 userId 반환
    SignupResponse signup(SignupRequest request);
    ProfileSetupResponse setupProfile(Long userId, ProfileSetupRequest request);
    UserDetailResponse getMemberProfile(Long userId);
    UpdateMemberResponse updateMember(Long userId, UpdateMemberRequest request);

}