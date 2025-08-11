package stparta300.snapi.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileSetupRequest {

    @Schema(description = "사용자가 설정할 닉네임", example = "UserNick")
    @NotBlank(message = "nickname은 필수입니다.")
    private String nickname;

    @Schema(description = "사용자 이메일", example = "test@naver.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "email은 필수입니다.")
    private String email;

    @Schema(description = "성별 (남자/여자)", example = "남자")
    @NotBlank(message = "gender는 필수입니다.")
    private String gender; // "남자" | "여자"

    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "2000-01-01")
    @NotBlank(message = "birth는 필수입니다.")
    private String birth;  // 문자열로 받아서 서비스에서 LocalDate 파싱

    @Schema(description = "약관 동의 여부", example = "true")
    @NotNull(message = "term은 필수입니다.")
    private Boolean term;
}