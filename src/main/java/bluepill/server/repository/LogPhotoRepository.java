package bluepill.server.repository;

import bluepill.server.domain.LogPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogPhotoRepository extends JpaRepository<LogPhoto, Long>, LogPhotoRepositoryCustom {

    Optional<LogPhoto> findByPublicId(UUID publicId);
}
