package stparta300.snapi.global.error.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    // 멤버
    LOGIN_SUCCESS(HttpStatus.OK, "MEMBER_SE2001", "회원 로그인이 성공되었습니다."),
    SIGNUP_SUCCESS(org.springframework.http.HttpStatus.OK, "MEMBER_SE2002", "회원가입 성공"),
    PROFILE_SETUP_SUCCESS(HttpStatus.OK, "MEMBER_SE2003", "프로필 설정 완료"),
    MEMBER_PROFILE_READ_SUCCESS(HttpStatus.OK, "MEMBER_SE2005", "회원 프로필 조회 성공"),
    MEMBER_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER_SE2006", "회원정보 수정 완료"),
    MEMBER_POINT_READ_SUCCESS(HttpStatus.OK, "MEMBER_SE2010", "포인트 내역 조회 성공"),
    MEMBER_POINT_HISTORY_SUCCESS(HttpStatus.OK, "200", "포인트 내역 조회 성공"),
    CHALLENGE_ACTIVE_LIST_SUCCESS(HttpStatus.OK, "200", "참여 중인 챌린지 목록 조회 성공"),
    CHALLENGE_LIST_SUCCESS(HttpStatus.OK, "200", "전체 챌린지 목록 조회 성공"),
    CHALLENGE_JOIN_SUCCESS(HttpStatus.OK, "200", "챌린지 참여 완료"),
    CHALLENGE_COMPLETE_SUCCESS(HttpStatus.OK, "200", "챌린지 완료 처리 성공"),
    CHALLENGE_DETAIL_SUCCESS(HttpStatus.OK, "200", "챌린지 상세 조회 성공"),
    MISSION_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "200", "미션 조회 성공"),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
