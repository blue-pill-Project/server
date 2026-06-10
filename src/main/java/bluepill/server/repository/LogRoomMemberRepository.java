package bluepill.server.repository;

import bluepill.server.domain.LogRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogRoomMemberRepository extends JpaRepository<LogRoomMember, Long> {

    boolean existsByLogRoom_IdAndUser_UserId(Long logRoomId, Long userId);

    Optional<LogRoomMember> findByPublicId(UUID publicId);

    Optional<LogRoomMember> findByLogRoom_IdAndUser_UserId(Long logRoomId, Long userId);
}
