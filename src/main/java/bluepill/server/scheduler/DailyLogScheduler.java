package bluepill.server.scheduler;

import bluepill.server.client.AgentClient;
import bluepill.server.client.AgentClient.AgentDailyLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLogScheduler {

    private final AgentClient agentClient;

    // 3시간 간격: 0,3,6,9,12,15,18,21시 정각, KST
    @Scheduled(cron = "0 0 0,3,6,9,12,15,18,21 * * *", zone = "Asia/Seoul")
    public void run() {
        // 현재 시각을 3시간 경계로 내림, 항상 유효 슬롯(0,3,6,9,12,15,18,21)
        int hour = LocalTime.now(ZoneId.of("Asia/Seoul")).getHour();
        String timeslot = String.valueOf(hour - (hour % 3));

        // TODO: 하드코딩된 대상(logRoom=1, user=1, member=1) 대신
        //           DB에서 활성 멤버(LogRoomMember)를 순회하며 각각 호출하도록 교체.
        var res = agentClient.generateDailyLog(
                new AgentDailyLogRequest(timeslot, 1L, 1L, 1L));  // (timeslot, logRoomId, userId, logRoomMemberId)
        log.info("daily-log 생성: timeslot={}, title={}", timeslot, res.title());
    }
}
