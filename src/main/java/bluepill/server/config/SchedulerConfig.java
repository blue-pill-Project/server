package bluepill.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 기반 스케줄링 활성화.
 * DailyLogScheduler 의 cron 을 켜면 이 설정으로 동작한다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
