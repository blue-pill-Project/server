package bluepill.server.repository;

import java.time.Instant;
import java.util.UUID;

public record LogRoomPageRow(
        Long roomId,
        UUID publicId,
        String name,
        Instant createdAt,
        Long creatorUserId,
        UUID creatorPublicId,
        String creatorNickname
) {}
