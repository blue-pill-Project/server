package bluepill.server.service;

import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.ExampleDialogue;
import bluepill.server.domain.User;
import bluepill.server.dto.character.CharacterCardCreateRequest;
import bluepill.server.dto.character.CharacterCardDetailResponse;
import bluepill.server.dto.character.CharacterCardListItem;
import bluepill.server.dto.character.CharacterCardListResponse;
import bluepill.server.dto.character.CharacterCardUpdateRequest;
import bluepill.server.dto.character.CharacterSortType;
import bluepill.server.dto.character.UserCharacterCardListItem;
import bluepill.server.dto.character.UserCharacterCardListResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.character.CharacterCardRepository;
import bluepill.server.repository.character.CharacterSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterCardService {

    private final CharacterCardRepository characterCardRepository;
    private final UserDailyLimitService userDailyLimitService;
    private final UserService userService;
    private final CharacterSnapshotRepository characterSnapshotRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public CharacterCard createCharacterCard(CharacterCardCreateRequest request, User creator) {
        // 일일 제한 체크 + 카운트 증가
        userDailyLimitService.increaseCharacterCreateCount(creator);

        // TODO(선택): imageUrl key 유효성 검증 (존재/소유 확인, INVALID_IMAGE_KEY)

        CharacterCard card = CharacterCard.builder()
                .publicId(UUID.randomUUID())
                .name(request.getName())
                .code(generateUniqueCode())
                .description(request.getDescription())
                .prompt(request.getPrompt())
                .imageUrl(request.getImageUrl())
                .isPublic(request.getIsPublic())
                .creator(creator)
                .build();

        if (request.getExampleDialogues() != null) {
            request.getExampleDialogues().forEach(content -> {
                ExampleDialogue dialogue = ExampleDialogue.builder()
                        .characterCard(card)
                        .content(content)
                        .build();
                card.addExampleDialogue(dialogue);
            });
        }

        return characterCardRepository.save(card);
    }

    @Transactional
    public void deleteCharacterCard(UUID publicId, Long userId) {
        CharacterCard card = characterCardRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        if (!card.getCreator().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_FORBIDDEN);
        }

        card.softDelete();
    }

    @Transactional
    public void updateCharacterCard(UUID publicId, Long userId, CharacterCardUpdateRequest request) {
        // TODO(선택): imageUrl key 유효성 검증

        CharacterCard card = characterCardRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        if (!card.getCreator().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_UPDATE_FORBIDDEN);
        }

        String oldImageKey = card.getImageUrl();

        card.update(
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                request.getPrompt(),
                request.getIsPublic()
        );

        if (request.getExampleDialogues() != null) {
            card.replaceExampleDialogues(request.getExampleDialogues());
        }

        if (request.hasContentChanges()) {
            card.incrementVersion();
        }

        // 이미지가 바뀌었으면, 옛 이미지를 참조하는 스냅샷이 없을 때만 R2에서 삭제 (커밋 후)
        String newImageKey = request.getImageUrl();
        if (oldImageKey != null && !oldImageKey.equals(newImageKey)) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    if (characterSnapshotRepository.existsByImageUrl(oldImageKey)) {
                        return;  // 스냅샷이 아직 참조 중이면 삭제하면 안 됨
                    }
                    try {
                        imageStorageService.deleteImage(oldImageKey);
                    } catch (Exception e) {
                        log.warn("옛 캐릭터 이미지 R2 삭제 실패(고아 남음): key={}", oldImageKey, e);
                    }
                }
            });
        }
    }

    @Transactional
    public void updateVisibility(UUID publicId, Long userId, Boolean isPublic) {
        CharacterCard card = characterCardRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        if (!card.getCreator().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_VISIBILITY_FORBIDDEN);
        }

        card.updateVisibility(isPublic);
    }

    public CharacterCardListResponse getLibrary(Long viewerId, String keyword, CharacterSortType sort,
                                                UUID cursor, int size) {
        List<CharacterCardListItem> result = characterCardRepository.findLibrary(
                viewerId, keyword, sort, cursor, size);

        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }

        UUID nextCursor = hasNext
                ? result.get(result.size() - 1).getPublicId()
                : null;

        long total = characterCardRepository.countLibrary(viewerId, keyword);

        return new CharacterCardListResponse(result, nextCursor, hasNext, total);
    }

    public CharacterCardDetailResponse getDetail(UUID publicId, Long viewerId) {
        CharacterCard card = characterCardRepository.findDetailByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        boolean isOwner = (viewerId != null)
                && card.getCreator().getUserId().equals(viewerId);

        // 비공개 카드인데 본인 아님 → 403
        if (!card.getIsPublic() && !isOwner) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_PRIVATE);
        }

        if (isOwner) {
            List<String> exampleDialogueContents = card.getExampleDialogues().stream()
                    .map(ExampleDialogue::getContent)
                    .toList();
            return CharacterCardDetailResponse.forOwner(card, exampleDialogueContents);
        }

        return CharacterCardDetailResponse.forViewer(card);
    }

    public UserCharacterCardListResponse getUserCharacterCards(
            UUID targetUserPublicId, Long viewerId, UUID cursor, int size) {

        User target = userService.findByPublicId(targetUserPublicId); //없거나 탈퇴면 USER_NOT_FOUND
        boolean isOwner = (viewerId != null) && target.getUserId().equals(viewerId);

        // 본인이면 비공개도 포함
        List<UserCharacterCardListItem> result = characterCardRepository.findByCreator(
                target.getUserId(), isOwner, cursor, size);

        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }

        UUID nextCursor = hasNext
                ? result.get(result.size() - 1).getPublicId()
                : null;

        long total = isOwner
                ? characterCardRepository.countByCreatorAndIsDeletedFalse(target)
                : characterCardRepository.countByCreatorAndIsDeletedFalseAndIsPublicTrue(target);

        return new UserCharacterCardListResponse(result, nextCursor, hasNext, total);
    }

    private Long generateUniqueCode() {
        Long code;
        do {
            code = ThreadLocalRandom.current().nextLong(10_000L, 1_000_000L);
        } while (characterCardRepository.existsByCode(code));
        return code;
    }
}
