package bluepill.server.repository.chat;

import bluepill.server.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

    // 방 삭제용: 방의 채팅 메시지 일괄 삭제
    @Modifying
    @Query("delete from ChatMessage c where c.logRoom.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
