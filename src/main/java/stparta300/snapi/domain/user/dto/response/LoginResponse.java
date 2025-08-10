package stparta300.snapi.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    @Schema(description = "사용자 ID (username과 별개)", example = "1")
    private Long userId;
}