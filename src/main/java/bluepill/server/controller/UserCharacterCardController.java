package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.character.UserCharacterCardListResponse;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.service.CharacterCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users/{publicId}/character-cards")
@RequiredArgsConstructor
public class UserCharacterCardController {

    private final CharacterCardService characterCardService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserCharacterCardListResponse>> getUserCharacterCards(
            @CurrentUserId Long viewerId,
            @PathVariable UUID publicId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") int size) {

        UserCharacterCardListResponse response = characterCardService.getUserCharacterCards(
                publicId, viewerId, cursor, size);

        return ResponseEntity.ok(
                ApiResponse.success("사용자 캐릭터 카드 목록 조회 성공", response));
    }
}
