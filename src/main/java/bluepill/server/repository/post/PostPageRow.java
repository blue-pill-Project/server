package bluepill.server.repository.post;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PostPageRow(
        Long postId,
        UUID publicId,
        LocalDate postDate,
        Integer timeSlot,
        Instant createdAt,
        Long sharerUserId,
        UUID sharerPublicId,
        String sharerNickname,
        String sharerProfileImageUrl
) {}
