package bluepill.server.exception;

import bluepill.server.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BusinessException 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException 발생: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    // 그 외 알 수 없는 에러 처리 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // DTO 유효성 검사 실패 시 @Valid, @Validated 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 첫 번째 에러만 가져옴. (여러 개여도 하나만 보여줌)
        FieldError fieldError = e.getBindingResult().getFieldError();

        // 에러는 발생했는데 세부 정보가 없는 예외적인 경우
        if (fieldError == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER));
        }

        // 어떤 필드에서 에러가 났는지, 어떤 종류의 에러인지 확인
        String field = fieldError.getField();   // "nickname", "termsAgreed" ...
        String code = fieldError.getCode();     // "Pattern", "NotBlank", "AssertTrue" ...

        // 로깅 및 응답 반환
        log.warn("Validation Error - Field: {}, Code: {}, Message: {}", field, code, fieldError.getDefaultMessage());

        // 에러 코드 매핑 (직접 정의한 ErrorCode Enum으로 변환)
        ErrorCode errorCode = mapToErrorCode(field, code);

        // 매핑된 게 있다면 그 에러 코드와 메시지 그대로 리턴 (예: NICKNAME_INVALID_FORMAT)
        if (errorCode != ErrorCode.INVALID_PARAMETER) {
            return ResponseEntity.status(errorCode.getHttpStatus())
                    .body(ApiResponse.error(errorCode));
        }

        // 매핑된 게 없을 경우. 코드는 INVALID_PARAMETER, 메시지는 DTO의 에러 메시지를 사용
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER, fieldError.getDefaultMessage()));
    }

    private ErrorCode mapToErrorCode(String field, String code) {
        // 닉네임 관련 에러
        if ("nickname".equals(field)) {
            // @NotBlank
            if ("NotBlank".equals(code)) {
                return ErrorCode.NICKNAME_MISSING;
            }
            // @Pattern
            if ("Pattern".equals(code)) {
                return ErrorCode.NICKNAME_INVALID_FORMAT;
            }
        }

        // 약관 동의 관련 에러
        if ("termsAgreed".equals(field)) {
            return ErrorCode.TERMS_NOT_AGREED;
        }

        // 캐릭터 카드 공개 여부 관련 에러
        if ("isPublic".equals(field) && "NotNull".equals(code)) {
            return ErrorCode.INVALID_INPUT;
        }

        // 그 외 알 수 없는 에러는 일반 파라미터 에러로 처리
        return ErrorCode.INVALID_PARAMETER;
    }

}
