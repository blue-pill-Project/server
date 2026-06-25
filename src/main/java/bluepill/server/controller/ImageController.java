package bluepill.server.controller;

import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.image.ImagePresignedResponse;
import bluepill.server.dto.image.ImagePresignedUrlRequest;
import bluepill.server.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imagestorageService;

    @Operation(summary = "이미지 업로드 presigned URL 발급")
    @PostMapping("/presigned-url")
    public ApiResponse<ImagePresignedResponse> getPresignedUrl(
            @Valid @RequestBody ImagePresignedUrlRequest request
            ){
        ImagePresignedResponse response = imagestorageService.createPresignedUrl(request.filename(), request.contentType());

        return ApiResponse.success("presigned URL 발급 성공", response);
    }
}
