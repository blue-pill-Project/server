package bluepill.server.dto.character;

import bluepill.server.domain.CharacterCard;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CharacterCardCreateResponse {

    private UUID publicId;
    private String name;
    private String description;
    private String imageUrl;
    private Instant createdAt;

    public static CharacterCardCreateResponse from(CharacterCard card) {
        return new CharacterCardCreateResponse(
                card.getPublicId(),
                card.getName(),
                card.getDescription(),
                card.getImageUrl(),
                card.getCreatedAt()
        );
    }
}
