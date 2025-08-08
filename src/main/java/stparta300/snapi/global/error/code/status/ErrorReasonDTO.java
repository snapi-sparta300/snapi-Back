package stparta300.snapi.global.error.code.status;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorReasonDTO {
    private HttpStatus httpStatus;

    private final boolean isSuccess;
    private final String code;
    private final String message;
    private final String detail;

    public static class ErrorReasonDTOBuilder {
        public ErrorReasonDTOBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }
    }

    public boolean getIsSuccess() { return isSuccess; }
}
