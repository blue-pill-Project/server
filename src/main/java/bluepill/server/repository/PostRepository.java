package bluepill.server.repository;

import bluepill.server.domain.LogRoom;
import bluepill.server.domain.Post;
import bluepill.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    Optional<Post> findByPublicId(UUID publicId);

    Long countByCreatedBy(User user);

    Long countByLogRoom(LogRoom logRoom);
}
