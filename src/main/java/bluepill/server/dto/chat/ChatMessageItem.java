package bluepill.server.dto.chat;

import java.time.Instant;

public record ChatMessageItem(
        String content,
        boolean isMe,
        Instant createdAt,
        String quotedPhotoUrl) {
}
