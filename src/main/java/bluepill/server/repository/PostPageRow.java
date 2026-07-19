package bluepill.server.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PostPageRow(
        Long postId,
        UUID publicId,
        Long roomId,
        UUID roomPublicId,
        String roomName,
        Long roomCreatorUserId,
        LocalDate postDate,
        Integer timeSlot,
        Instant createdAt,
        Long sharerUserId,
        UUID sharerPublicId,
        String sharerNickname,
        String sharerProfileImageUrl
) {}
