package bluepill.server.repository.chat;

import bluepill.server.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findMessages(Long logRoomId, Long cursorId, int size);
}
