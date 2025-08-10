package stparta300.snapi.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.response.LoginResponse;
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
}