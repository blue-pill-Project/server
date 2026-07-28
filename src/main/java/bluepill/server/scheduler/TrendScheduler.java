package bluepill.server.scheduler;

import bluepill.server.client.AgentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendScheduler {

    private final AgentClient agentClient;

    // 매월 1일 00:00 KST
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void run() {
        try {
            var res = agentClient.generateTrend();
            if (res.success()) {
                log.info("trend 저장 성공");
            } else {
                log.error("trend 저장 실패(success=false)");
            }
        } catch (Exception e) {
            log.error("trend 호출 실패", e);
        }
    }
}
