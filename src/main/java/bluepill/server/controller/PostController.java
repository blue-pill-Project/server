package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.post.PostListResponse;
import bluepill.server.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name="Post")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시물 목록 조회")
    @GetMapping()
    public ApiResponse<PostListResponse> getPosts(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUserId Long userId){

        PostListResponse response = postService.getPosts(cursor, size, userId);

        return ApiResponse.success("게시물 목록 조회 성공", response);
    }

    @Operation(summary = "게시물 삭제")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable UUID publicId, @CurrentUserId Long userId){
        postService.deletePost(publicId,userId);
        return ResponseEntity.ok(
                ApiResponse.success("게시물이 삭제되었습니다.")
        );
    }
}
