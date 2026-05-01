package bluepill.server.dto.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CharacterCardListItem {

    private UUID publicId;
    private String name;
    private Long characterCode;
    private Integer version;
    private String description;
    private String imageUrl;
    private UUID creatorPublicId;
    private String creatorNickname;
    private Long useCount;
    private Instant createdAt;
    private Instant updatedAt;
}
