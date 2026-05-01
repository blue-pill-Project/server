package bluepill.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 Bad Request
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 값을 확인해주세요."),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해야 합니다."),
    NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "닉네임은 12자 이내여야합니다."),
    NICKNAME_MISSING(HttpStatus.BAD_REQUEST, "닉네임은 필수 입력 값입니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 키입니다. 이미지 생성을 먼저 진행해주세요."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

    // 403 Forbidden
    ALREADY_REGISTERED(HttpStatus.FORBIDDEN, "이미 가입이 완료된 회원입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    CHARACTER_CARD_PRIVATE(HttpStatus.FORBIDDEN, "비공개 카드입니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    USER_NOT_FOUND_LOGOUT(HttpStatus.NOT_FOUND, "이미 로그아웃 된 사용자입니다."),
    ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 탈퇴한 사용자입니다."),
    CHARACTER_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 캐릭터 카드입니다."),

    // 409 Conflict
    NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),

    // 429 Too Many Requests
    CHARACTER_CREATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "일일 캐릭터 생성 횟수를 초과했습니다. 내일 다시 시도해주세요. (하루 3회 제한)"),

    // 500 Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러가 발생했습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name(); // "NICKNAME_DUPLICATION" 같은 이름 반환
    }

}
