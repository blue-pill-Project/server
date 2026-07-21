package bluepill.server.scheduler;

import bluepill.server.client.AgentClient;
import bluepill.server.client.AgentClient.AgentDailyLogRequest;
import bluepill.server.repository.logroom.LogRoomMemberRepository;
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
    private final LogRoomMemberRepository memberRepository;

    // 3시간 간격: 0,3,6,9,12,15,18,21시 정각, KST
    @Scheduled(cron = "0 0 0,3,6,9,12,15,18,21 * * *", zone = "Asia/Seoul")
    public void run() {
        // 현재 시각을 3시간 경계로 내림, 항상 유효 슬롯(0,3,6,9,12,15,18,21)
        int hour = LocalTime.now(ZoneId.of("Asia/Seoul")).getHour();
        String timeslot = String.valueOf(hour - (hour % 3));

        // 활성 캐릭터 멤버 전체를 순회하며 각각 에이전트 호출
        var targets = memberRepository.findActiveCharacterTargets();
        for (var t : targets) {
            try {
                var res = agentClient.generateDailyLog(new AgentDailyLogRequest(
                        timeslot, t.getLogRoomId(), t.getUserId(), t.getMemberId()));
                log.info("daily-log 생성: member={}, timeslot={}, title={}",
                        t.getMemberId(), timeslot, res.title());
            } catch (Exception e) {
                // 한 멤버 실패해도 나머지는 계속
                log.error("daily-log 실패: member={}", t.getMemberId(), e);
            }
        }
    }
}
