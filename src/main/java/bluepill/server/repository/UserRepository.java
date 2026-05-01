package bluepill.server.repository;

import bluepill.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface  UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicIdAndIsDeletedFalse(UUID publicId);
    Optional<User> findByProviderAndProviderId(User.Provider provider, String providerId);
}
