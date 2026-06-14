package bluepill.server.dto.chat;

import java.util.List;
import java.util.UUID;

public record ChatMessageListResponse(
        List<ChatMessageItem> messages,
        UUID nextCursor,
        boolean hasMore
) {
}
