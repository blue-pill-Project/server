package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogRoomListItem {
    private UUID publicId;
    private String name;
    private Long participantCount;
    private Instant createdAt;
    private Boolean isOwner;
    private UUID ownerPublicId;
    private String ownerNickname;
    private List<LogRoomParticipant> participants;
}
