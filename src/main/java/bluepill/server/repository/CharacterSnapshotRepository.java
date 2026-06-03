package bluepill.server.repository;

import bluepill.server.domain.CharacterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterSnapshotRepository extends JpaRepository<CharacterSnapshot, Long> {

    Optional<CharacterSnapshot> findByCharacterIdAndVersion(Long characterId, Integer version);
}
