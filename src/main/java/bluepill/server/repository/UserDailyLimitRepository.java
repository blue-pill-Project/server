package bluepill.server.repository;

import bluepill.server.domain.User;
import bluepill.server.domain.UserDailyLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserDailyLimitRepository extends JpaRepository<UserDailyLimit, Long> {

    Optional<UserDailyLimit> findByUserAndCreatedAtBetween(
            User user, Instant start, Instant end);
}
