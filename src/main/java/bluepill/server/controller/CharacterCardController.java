package bluepill.server.controller;

import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.User;
import bluepill.server.dto.character.CharacterCardCreateRequest;
import bluepill.server.dto.character.CharacterCardCreateResponse;
import bluepill.server.dto.character.CharacterCardDetailResponse;
import bluepill.server.dto.character.CharacterCardListResponse;
import bluepill.server.dto.character.CharacterCardUpdateRequest;
import bluepill.server.dto.character.CharacterCardVisibilityRequest;
import bluepill.server.dto.character.CharacterSortType;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.service.CharacterCardService;
import bluepill.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/character-cards")
@RequiredArgsConstructor
public class CharacterCardController {

    private final CharacterCardService characterCardService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CharacterCardCreateResponse>> createCharacterCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CharacterCardCreateRequest request) {

        // TODO: 인증 구현 확정 후 검토. 현재 가정: JWT sub = users.id (internal BIGINT)
        Long userId = Long.parseLong(userDetails.getUsername());
        User creator = userService.findById(userId);

        CharacterCard card = characterCardService.createCharacterCard(request, creator);
        CharacterCardCreateResponse response = CharacterCardCreateResponse.from(card);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("캐릭터 카드 생성 성공", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CharacterCardListResponse>> getLibrary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") int size) {

        CharacterSortType sortType = CharacterSortType.from(sort);
        CharacterCardListResponse response = characterCardService.getLibrary(
                keyword, sortType, cursor, size);

        return ResponseEntity
                .ok(ApiResponse.success("라이브러리 캐릭터 목록 조회 성공", response));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<CharacterCardDetailResponse>> getCharacterCardDetail(
            @AuthenticationPrincipal(errorOnInvalidType = false) UserDetails userDetails,
            @PathVariable UUID publicId) {

        Long viewerId = (userDetails != null)
                ? Long.parseLong(userDetails.getUsername())
                : null;

        CharacterCardDetailResponse response = characterCardService.getDetail(publicId, viewerId);

        return ResponseEntity.ok(
                ApiResponse.success("캐릭터 카드 상세 조회 성공", response));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteCharacterCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID publicId) {

        Long userId = Long.parseLong(userDetails.getUsername());
        characterCardService.deleteCharacterCard(publicId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("캐릭터 카드가 성공적으로 삭제되었습니다."));
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> updateCharacterCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid CharacterCardUpdateRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        characterCardService.updateCharacterCard(publicId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("캐릭터 카드가 성공적으로 수정되었습니다."));
    }

    @PatchMapping("/{publicId}/visibility")
    public ResponseEntity<ApiResponse<Void>> updateVisibility(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid CharacterCardVisibilityRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        characterCardService.updateVisibility(publicId, userId, request.getIsPublic());

        return ResponseEntity.ok(
                ApiResponse.success("공개 여부가 성공적으로 변경되었습니다."));
    }
}
