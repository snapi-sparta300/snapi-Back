package stparta300.snapi.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @Schema(description = "사용자가 등록할 아이디", example = "newUser")
    @NotBlank(message = "username은 필수입니다.")
    private String username;

    @Schema(description = "사용자가 등록할 비밀번호", example = "password123")
    @NotBlank(message = "password는 필수입니다.")
    private String password;
}