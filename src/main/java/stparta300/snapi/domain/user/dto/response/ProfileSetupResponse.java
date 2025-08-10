package stparta300.snapi.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileSetupResponse {

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "UserNick")
    private String nickname;

    @Schema(description = "사용자 이메일", example = "test@naver.com")
    private String email;

    @Schema(description = "성별", example = "남자")
    private String gender;

    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "2000-01-01")
    private String birth;

    @Schema(description = "사용자 포인트", example = "0")
    private Long userPoint;
}