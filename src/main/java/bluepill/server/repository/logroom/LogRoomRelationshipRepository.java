package bluepill.server.repository.logroom;

import bluepill.server.domain.LogRoomRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRoomRelationshipRepository extends JpaRepository<LogRoomRelationship, Long> {
}
