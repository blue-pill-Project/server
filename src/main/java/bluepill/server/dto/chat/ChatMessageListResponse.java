package bluepill.server.dto.chat;

import java.util.List;

public record ChatMessageListResponse(
        List<ChatMessageItem> messages,
        Long nextCursor,
        boolean hasMore
) {
}
