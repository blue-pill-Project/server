package bluepill.server.controller;

import bluepill.server.client.AgentClient;
import bluepill.server.client.AgentClient.AgentDailyLogRequest;
import bluepill.server.client.AgentClient.AgentDailyLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * [개발용] daily-logs-agent 를 수동으로 호출해 파이프를 검증하는 엔드포인트.
 * 스케줄러 cron 을 켜기 전, Spring → 에이전트 연결이 되는지 확인하는 용도.
 * 나중에 실서비스에선 제거하거나 스케줄러로 대체.
 */
@Tag(name = "Dev - Daily Log Trigger")
@Profile("local")   // 로컬 프로파일에서만 등록 (프로덕션엔 존재하지 않음)
@RestController
@RequestMapping("/api/dev/daily-log")
@RequiredArgsConstructor
public class DailyLogTriggerController {

    private final AgentClient agentClient;

    @Operation(summary = "[개발용] daily-log 에이전트 수동 호출")
    @PostMapping("/trigger")
    public AgentDailyLogResponse trigger(
            @RequestParam(defaultValue = "6") String timeslot,
            @RequestParam(defaultValue = "1") Long logRoomId,
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "1") Long logRoomMemberId
    ) {
        return agentClient.generateDailyLog(
                new AgentDailyLogRequest(timeslot, logRoomId, userId, logRoomMemberId));
    }
}
