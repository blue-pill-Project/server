package bluepill.server.dto.common;

import bluepill.server.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "응답 코드", example = "SUCCESS")
    private String code;

    @Schema(description = "응답 메시지", example = "요청이 성공하였습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    // ErrorCode 메시지 그대로 사용
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    // 메세지 커스텀 해서 내보냄
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(errorCode.getCode(), customMessage, null);
    }
}