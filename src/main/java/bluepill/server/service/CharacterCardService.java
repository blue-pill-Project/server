package bluepill.server.service;

import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.ExamplePost;
import bluepill.server.domain.User;
import bluepill.server.dto.character.CharacterCardCreateRequest;
import bluepill.server.dto.character.CharacterCardDetailResponse;
import bluepill.server.dto.character.CharacterCardListItem;
import bluepill.server.dto.character.CharacterCardListResponse;
import bluepill.server.dto.character.CharacterSortType;
import bluepill.server.dto.character.UserCharacterCardListItem;
import bluepill.server.dto.character.UserCharacterCardListResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.CharacterCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterCardService {

    private final CharacterCardRepository characterCardRepository;
    private final UserDailyLimitService userDailyLimitService;
    private final UserService userService;

    @Transactional
    public CharacterCard createCharacterCard(CharacterCardCreateRequest request, User creator) {
        // 일일 제한 체크 + 카운트 증가
        userDailyLimitService.increaseCharacterCreateCount(creator);

        // TODO: imageUrl(S3 temp key) 유효성 검증 (INVALID_IMAGE_KEY)
        // TODO: temp/ → characters/ 이동
        //  @Transactional은 S3 작업을 롤백 못 함.
        //  @TransactionalEventListener(AFTER_COMMIT)로 트랜잭션 커밋 후 S3 이동 +
        //  별도 트랜잭션으로 imageUrl 업데이트.

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

        if (request.getExamplePosts() != null) {
            request.getExamplePosts().forEach(content -> {
                ExamplePost post = ExamplePost.builder()
                        .characterCard(card)
                        .content(content)
                        .build();
                card.addExamplePost(post);
            });
        }

        return characterCardRepository.save(card);
    }

    public CharacterCardListResponse getLibrary(String keyword, CharacterSortType sort,
                                                UUID cursor, int size) {
        List<CharacterCardListItem> result = characterCardRepository.findLibrary(
                keyword, sort, cursor, size);

        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }

        UUID nextCursor = hasNext
                ? result.get(result.size() - 1).getPublicId()
                : null;

        return new CharacterCardListResponse(result, nextCursor, hasNext);
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
            List<String> examplePostContents = card.getExamplePosts().stream()
                    .map(ExamplePost::getContent)
                    .toList();
            return CharacterCardDetailResponse.forOwner(card, examplePostContents);
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

        return new UserCharacterCardListResponse(result, nextCursor, hasNext);
    }

    private Long generateUniqueCode() {
        Long code;
        do {
            code = ThreadLocalRandom.current().nextLong(10_000L, 1_000_000L);
        } while (characterCardRepository.existsByCode(code));
        return code;
    }
}
