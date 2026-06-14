package bluepill.server.dto.chat;

import java.time.Instant;

public record ChatMessageResponse(
        String content,
        boolean isMe,
        Instant createdAt
) {
}
