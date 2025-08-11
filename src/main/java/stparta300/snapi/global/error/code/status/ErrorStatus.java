package stparta300.snapi.global.error.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    LOGIN_BAD_CREDENTIALS(HttpStatus.BAD_REQUEST, "LOGIN400", "아이디 또는 비밀번호가 틀렸습니다."),
    SIGNUP_USERNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP400", "회원가입 실패. 아이디 중복"),
    PROFILE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PROFILE400", "프로필 설정 실패"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "사용자를 찾을 수 없습니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER400_EMAIL", "이미 사용 중인 이메일입니다."),
    MEMBER_UPDATE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MEMBER400", "회원정보 수정 실패"),
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "대상을 찾을 수 없습니다."), // 챌린지 미존재
    CHALLENGE_CAPACITY_FULL(HttpStatus.CONFLICT, "409", "챌린지 정원이 가득 찼습니다."),
    CHALLENGE_ALREADY_JOINED(HttpStatus.CONFLICT, "409", "이미 참여 중인 챌린지입니다."),
    USER_CHALLENGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "400", "잘못된 요청입니다."),
    CHALLENGE_NOT_ALL_MISSIONS_DONE(HttpStatus.CONFLICT, "409", "모든 미션을 완료하지 않아 챌린지를 완료할 수 없습니다."),
    CHALLENGE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "409", "이미 완료된 챌린지입니다."),// (선택)
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
