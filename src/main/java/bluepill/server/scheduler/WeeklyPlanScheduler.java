package bluepill.server.scheduler;

import bluepill.server.client.AgentClient;
import bluepill.server.client.AgentClient.AgentWeeklyPlanRequest;
import bluepill.server.repository.logroom.LogRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyPlanScheduler {

    private final AgentClient agentClient;
    private final LogRoomMemberRepository memberRepository;

    // 매주 일요일 23:00 KST — agent 가 다가오는 주(월~일)를 계획, 월요일 00:00 전에 저장 완료
    @Scheduled(cron = "0 0 23 * * SUN", zone = "Asia/Seoul")
    public void run() {
        var targets = memberRepository.findActiveCharacterTargets();
        for (var t : targets) {
            try {
                var res = agentClient.generateWeeklyPlan(new AgentWeeklyPlanRequest(
                        String.valueOf(t.getUserId()),
                        String.valueOf(t.getLogRoomId()),
                        String.valueOf(t.getMemberId())));
                if (res.success()) {
                    log.info("weekly-plan 저장 성공: member={}", t.getMemberId());
                } else {
                    log.error("weekly-plan 저장 실패(success=false): member={}", t.getMemberId());
                }
            } catch (Exception e) {
                log.error("weekly-plan 호출 실패: member={}", t.getMemberId(), e);
            }
        }
    }
}
