package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogCharacterCardResponse {
    private UUID memberPublicId;
    private UUID characterPublicId;
    private String name;
    private String description;
    private String imageUrl;
    private Long useCount;
    private Boolean isDeleted;
    private Boolean isPublic;
    private Boolean isLatest;
    private Boolean isOwner;
    private Boolean canUpdate;
}
