package bluepill.server.repository.character;

import bluepill.server.domain.CharacterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterSnapshotRepository extends JpaRepository<CharacterSnapshot, Long> {

    Optional<CharacterSnapshot> findByCharacterIdAndVersion(Long characterId, Integer version);

    // 특정 이미지 key 를 참조하는 스냅샷이 존재하는지
    boolean existsByImageUrl(String imageUrl);
}
