package bluepill.server.repository.logroom;

import bluepill.server.domain.LogRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogRoomMemberRepository extends JpaRepository<LogRoomMember, Long> {

    boolean existsByLogRoom_IdAndUser_UserId(Long logRoomId, Long userId);

    Optional<LogRoomMember> findByPublicId(UUID publicId);

    Optional<LogRoomMember> findByLogRoom_IdAndUser_UserId(Long logRoomId, Long userId);

    Optional<LogRoomMember> findByLogRoom_IdAndSnapshotIsNotNull(Long logRoomId);

    long countByUser_UserId(Long userId);

    // 활성 캐릭터 멤버(스냅샷 있는) 전체 : daily-log 스케줄러가 순회할 대상
    @Query("""
            SELECT m.id AS memberId,
                   m.logRoom.id AS logRoomId,
                   m.logRoom.createdBy.userId AS userId
            FROM LogRoomMember m
            WHERE m.snapshot IS NOT NULL
            """)
    List<DailyLogTarget> findActiveCharacterTargets();

    // 방 삭제용: 방의 멤버 일괄 삭제
    @Modifying
    @Query("delete from LogRoomMember m where m.logRoom.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    // 조회 결과 projection
    interface DailyLogTarget {
        Long getMemberId();
        Long getLogRoomId();
        Long getUserId();
    }
}
