package stparta300.snapi.domain.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stparta300.snapi.domain.challenge.dto.response.ActiveChallengesResponse;
import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.dto.response.CompleteChallengeResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;
import stparta300.snapi.domain.challenge.service.ChallengeService;
import stparta300.snapi.domain.user.service.UserService;
import stparta300.snapi.global.common.response.ApiResponse;
import stparta300.snapi.global.error.code.status.SuccessStatus;

@Tag(name = "Challenge", description = "챌린지 API")
@RestController
@RequiredArgsConstructor
public class ChallengeController {

    private final UserService userService;
    private final ChallengeService challengeService;

    @Operation(summary = "참여 중인 챌린지 목록 조회",
            description = "특정 사용자의 진행중 챌린지 목록을 조회합니다.")
    @GetMapping("/challenges/{id}")
    public ApiResponse<ActiveChallengesResponse> getActiveChallenges(@PathVariable("id") Long userId) {
        return ApiResponse.onSuccess(
                SuccessStatus.CHALLENGE_ACTIVE_LIST_SUCCESS, // (200, "…", "참여 중인 챌린지 목록 조회 성공")
                userService.getActiveChallenges(userId)
        );
    }


    @Operation(summary = "전체 챌린지 목록 조회",
            description = "모든 챌린지 목록을 조회합니다.")
    @GetMapping("/challenges")
    public ApiResponse<ChallengeListResponse> getChallenges() {
        return ApiResponse.onSuccess(
                SuccessStatus.CHALLENGE_LIST_SUCCESS, // (200, "200", "전체 챌린지 목록 조회 성공")
                challengeService.getChallenges()
        );
    }

    @Operation(summary = "챌린지 참여하기", description = "사용자가 특정 챌린지에 참여 신청합니다.")
    @PostMapping("/challenges/{id}/{challengeId}")
    public ApiResponse<JoinChallengeResponse> join(
            @PathVariable("id") Long userId,
            @PathVariable Long challengeId
    ) {
        return ApiResponse.onSuccess(
                SuccessStatus.CHALLENGE_JOIN_SUCCESS, // (200, "챌린지 참여 완료")
                challengeService.join(userId, challengeId)
        );
    }

    @Operation(summary = "챌린지 완료 처리",
            description = "사용자가 참여 중인 챌린지를 완료 상태로 전환합니다.")
    @PatchMapping("/challenges/{id}/{challengeId}")
    public ApiResponse<CompleteChallengeResponse> complete(
            @PathVariable("id") Long userId,
            @PathVariable Long challengeId
    ) {
        return ApiResponse.onSuccess(
                SuccessStatus.CHALLENGE_COMPLETE_SUCCESS, // (200, "챌린지 완료 처리 성공")
                challengeService.complete(userId, challengeId)
        );
    }
}