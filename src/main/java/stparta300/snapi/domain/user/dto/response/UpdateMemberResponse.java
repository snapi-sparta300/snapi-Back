package stparta300.snapi.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateMemberResponse {
    @Schema(example = "1")           private Long userId;
    @Schema(example = "newUser")     private String userName;
    @Schema(example = "test@naver.com") private String email;
    @Schema(example = "새닉네임")    private String nickname;
    @Schema(example = "여자")        private String gender;  // "남자"/"여자"
    @Schema(example = "2006-03-11")  private String birth;   // YYYY-MM-DD
    @Schema(example = "true")        private Boolean term;
    @Schema(example = "0")           private Long userPoint;
}