package bluepill.server.repository;

import bluepill.server.domain.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findMessages(Long logRoomId, UUID cursorPublicId, int size);
}
