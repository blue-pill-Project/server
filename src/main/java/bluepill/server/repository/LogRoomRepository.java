package bluepill.server.repository;

import bluepill.server.domain.LogRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogRoomRepository extends JpaRepository<LogRoom, Long>, LogRoomRepositoryCustom {

    Optional<LogRoom> findByPublicId(UUID publicId);
}
