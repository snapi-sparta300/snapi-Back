// src/main/java/stparta300/snapi/domain/user/dto/response/UserDetailResponse.java
package stparta300.snapi.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {
    @Schema(example = "1")  private Long userId;
    @Schema(example = "UserNick") private String nickname;
    @Schema(example = "test@naver.com") private String email;
    @Schema(example = "남자") private String gender; // "남자"/"여자"
    @Schema(example = "2000-01-01") private String birth; // YYYY-MM-DD
    @Schema(example = "0")  private Long userPoint;
}