package stparta300.snapi.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    @Schema(description = "신규 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "신규 사용자 아이디", example = "newUser")
    private String username;
}