package bluepill.server.dto.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageItem(
        UUID publicId,
        String content,
        boolean isMe,
        Instant createdAt) {
}
