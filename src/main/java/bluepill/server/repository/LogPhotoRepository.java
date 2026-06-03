package bluepill.server.repository;

import bluepill.server.domain.LogPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogPhotoRepository extends JpaRepository<LogPhoto, Long>, LogPhotoRepositoryCustom {
}
