package stparta300.snapi.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;
import stparta300.snapi.domain.user.dto.request.UpdateMemberRequest;
import stparta300.snapi.domain.user.dto.response.*;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.service.UserService;
import stparta300.snapi.global.common.response.ApiResponse;
import stparta300.snapi.global.error.code.status.SuccessStatus;

@Tag(name = "members", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserConverter userConverter;

    @Operation(summary = "로그인", description = "username/password 인증 후 userId 반환")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Long userId = userService.login(request);
        return ApiResponse.onSuccess(
                SuccessStatus.LOGIN_SUCCESS,
                userConverter.toLoginResponse(userId)
        );
    }

    @Operation(summary = "회원가입", description = "username, password로 신규 사용자 등록")
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.onSuccess(
                SuccessStatus.SIGNUP_SUCCESS,  // 메시지: "회원가입 성공"
                userService.signup(request)
        );
    }

    @Operation(summary = "회원가입(프로필설정)", description = "닉네임/이메일/성별/생년월일/약관동의 설정")
    @PostMapping("/signup/{id}")
    public ApiResponse<ProfileSetupResponse> setupProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProfileSetupRequest request
    ) {
        return ApiResponse.onSuccess(
                SuccessStatus.PROFILE_SETUP_SUCCESS, // "프로필 설정 완료"
                userService.setupProfile(id, request)
        );
    }

    @Operation(summary = "회원정보조회(전체)", description = "단일 회원의 전체 프로필 정보를 조회합니다.")
    @GetMapping("/members/{id}/profile")
    public ApiResponse<UserDetailResponse> getMemberProfile(@PathVariable("id") Long id) {
        return ApiResponse.onSuccess(
                SuccessStatus.MEMBER_PROFILE_READ_SUCCESS, // (200, "MEMBER_SE2005", "회원 프로필 조회 성공") 등으로 등록
                userService.getMemberProfile(id)
        );
    }

    @Operation(summary = "회원정보 수정(부분)", description = "nickname/email/gender/birth/term 중 전달된 필드만 수정합니다.")
    @PatchMapping("/members/{id}")
    public ApiResponse<UpdateMemberResponse> updateMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ApiResponse.onSuccess(
                SuccessStatus.MEMBER_UPDATE_SUCCESS, // (200, "…", "회원정보 수정 완료")
                userService.updateMember(id, request)
        );
    }
}