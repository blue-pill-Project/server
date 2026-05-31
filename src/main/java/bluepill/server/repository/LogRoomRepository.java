package bluepill.server.repository;

import bluepill.server.domain.LogRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRoomRepository extends JpaRepository<LogRoom, Long>, LogRoomRepositoryCustom {
}
