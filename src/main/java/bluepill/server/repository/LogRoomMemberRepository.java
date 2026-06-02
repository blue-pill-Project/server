package bluepill.server.repository;

import bluepill.server.domain.LogRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRoomMemberRepository extends JpaRepository<LogRoomMember, Long> {

    boolean existsByLogRoom_IdAndUser_UserId(Long logRoomId, Long userId);
}
