package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserDailyLimit;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.UserDailyLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDailyLimitService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CHARACTER_CREATE_DAILY_LIMIT = 30;

    private final UserDailyLimitRepository userDailyLimitRepository;

    @Transactional
    public void increaseCharacterCreateCount(User user) {
        UserDailyLimit limit = findOrCreateTodayLimit(user);

        if (limit.getCharacterCreateCount() >= CHARACTER_CREATE_DAILY_LIMIT) {
            throw new BusinessException(ErrorCode.CHARACTER_CREATE_LIMIT_EXCEEDED);
        }

        limit.increaseCharacterCreateCount();
    }

    private UserDailyLimit findOrCreateTodayLimit(User user) {
        LocalDate today = LocalDate.now(KST);
        Instant start = today.atStartOfDay(KST).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(KST).minusNanos(1).toInstant();

        return userDailyLimitRepository
                .findByUserAndCreatedAtBetween(user, start, end)
                .orElseGet(() -> userDailyLimitRepository.save(
                        UserDailyLimit.builder().user(user).build()
                ));
    }
}
