package bluepill.server.repository.logroom;

import bluepill.server.domain.LogRoomRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogRoomRelationshipRepository extends JpaRepository<LogRoomRelationship, Long> {

    // 방 삭제용: 방의 관계 일괄 삭제
    @Modifying
    @Query("delete from LogRoomRelationship r where r.logRoom.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
