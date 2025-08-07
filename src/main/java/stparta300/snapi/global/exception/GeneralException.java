package stparta300.snapi.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stparta300.snapi.global.error.code.status.BaseErrorCode;
import stparta300.snapi.global.error.code.status.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private BaseErrorCode code;
    public ErrorReasonDTO getErrorReason() {
        return this.code.getReason();
    }
    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}