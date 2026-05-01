package bluepill.server.dto.character;

import bluepill.server.domain.CharacterCard;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CharacterCardDetailResponse {

    private UUID publicId;
    private String name;
    private Long characterCode;
    private Integer version;
    private String description;
    private String imageUrl;
    private UUID creatorPublicId;
    private String creatorNickname;

    // 본인 생성 카드일 때만 채워짐 (아니면 null → JSON 직렬화 시 누락됨)
    private String prompt;
    private List<String> examplePosts;
    private Boolean isPublic;

    private Long useCount;
    private Instant createdAt;
    private Instant updatedAt;

    //본인 카드
    public static CharacterCardDetailResponse forOwner(CharacterCard card, List<String> examplePostContents) {
        return new CharacterCardDetailResponse(
                card.getPublicId(),
                card.getName(),
                card.getCode(),
                card.getVersion(),
                card.getDescription(),
                card.getImageUrl(),
                card.getCreator().getPublicId(),
                card.getCreator().getNickname(),
                card.getPrompt(),
                examplePostContents,
                card.getIsPublic(),
                card.getUseCnt(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    //다른 사람 카드
    public static CharacterCardDetailResponse forViewer(CharacterCard card) {
        return new CharacterCardDetailResponse(
                card.getPublicId(),
                card.getName(),
                card.getCode(),
                card.getVersion(),
                card.getDescription(),
                card.getImageUrl(),
                card.getCreator().getPublicId(),
                card.getCreator().getNickname(),
                null,    // prompt
                null,    // examplePosts
                null,    // isPublic
                card.getUseCnt(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
