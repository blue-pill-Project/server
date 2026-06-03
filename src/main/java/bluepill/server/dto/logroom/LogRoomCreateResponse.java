package bluepill.server.dto.logroom;

import bluepill.server.domain.LogRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogRoomCreateResponse {

    private UUID publicId;
    private String name;
    private Boolean isPublic;
    private Instant createdAt;

    public static LogRoomCreateResponse from(LogRoom room) {
        return new LogRoomCreateResponse(
                room.getPublicId(),
                room.getName(),
                room.getIsPublic(),
                room.getCreatedAt()
        );
    }
}
