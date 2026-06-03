package bluepill.server.repository;

import bluepill.server.domain.LogRoomRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRoomRelationshipRepository extends JpaRepository<LogRoomRelationship, Long> {
}
