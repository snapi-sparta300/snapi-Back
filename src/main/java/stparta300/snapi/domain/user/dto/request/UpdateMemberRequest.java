package stparta300.snapi.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class UpdateMemberRequest {

    @Schema(description = "닉네임", example = "새닉네임")
    private String nickname;  // optional

    @Schema(description = "이메일", example = "test@naver.com")
    private String email;     // optional

    @Schema(description = "성별(남자/여자)", example = "여자")
    private String gender;    // optional: "남자" | "여자"

    @Schema(description = "생년월일(YYYY-MM-DD)", example = "2006-03-11")
    private String birth;     // optional: "YYYY-MM-DD"

    @Schema(description = "약관 동의 여부", example = "true")
    private Boolean term;     // optional
}