package stparta300.snapi.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "사용자 아이디(userName에 매핑)", example = "test")
    @NotBlank(message = "username은 필수입니다.")
    private String username;

    @Schema(description = "비밀번호", example = "test")
    @NotBlank(message = "password는 필수입니다.")
    private String password;
}