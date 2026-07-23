package bluepill.server.dto.character;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserCharacterCardListItem {

    private UUID publicId;
    private String name;
    private Long characterCode;
    private String description;
    @Setter
    private String imageUrl;
    private Long useCount;
    private Instant createdAt;
    private Instant updatedAt;
}
