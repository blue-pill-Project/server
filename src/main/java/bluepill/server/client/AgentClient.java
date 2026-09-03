package bluepill.server.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * daily-logs-agent (FastAPI) 를 HTTP로 호출하는 클라이언트.
 * 주소는 application.yaml 의 agent.base-url 에서 주입 (로컬: http://localhost:8000).
 */
@Component
public class AgentClient {

    private final RestClient restClient;

    public AgentClient(@Value("${agent.base-url}") String baseUrl) {
        // 에이전트 호출은 LLM 여러 번 + 이미지 생성이라 수십 초~수 분 걸림.
        // 기본 read timeout 으로는 끊기므로 5분으로 설정.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofMinutes(5));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public AgentDailyLogResponse generateDailyLog(AgentDailyLogRequest req) {
        return restClient.post()
                .uri("/daily-logs/run")
                .body(req)
                .retrieve()
                .body(AgentDailyLogResponse.class);
    }

    public AgentTrendResponse generateTrend() {
        return restClient.post()
                .uri("/trend/run")
                .retrieve()
                .body(AgentTrendResponse.class);
    }

    public AgentWeeklyPlanResponse generateWeeklyPlan(AgentWeeklyPlanRequest req) {
        return restClient.post()
                .uri("/weekly-plan/run")
                .body(req)
                .retrieve()
                .body(AgentWeeklyPlanResponse.class);
    }

    // 방 삭제 시 agent 쪽 데이터(daily_plans + LangGraph 기억) 정리 요청
    public AgentLogRoomDeleteResponse deleteLogRoomData(Long logRoomId) {
        return restClient.delete()
                .uri("/log-rooms/{id}", logRoomId)
                .retrieve()
                .body(AgentLogRoomDeleteResponse.class);
    }

    // 캐릭터 프롬프트 자동완성
    public AgentCharacterPromptResponse completeCharacterPrompt(AgentCharacterPromptRequest req) {
        return restClient.post()
                .uri("/character-prompt/complete")
                .body(req)
                .retrieve()
                .body(AgentCharacterPromptResponse.class);
    }

    // chat_rule 생성
    public AgentChatRuleResponse completeChatRule(AgentChatRuleRequest req) {
        return restClient.post()
                .uri("/chat-rule/run")
                .body(req)
                .retrieve()
                .body(AgentChatRuleResponse.class);
    }

    public record AgentDailyLogRequest(
            String timeslot,
            @JsonProperty("user_id") String userId,
            @JsonProperty("log_room_id") String logRoomId,
            @JsonProperty("log_room_member_id") String logRoomMemberId
    ) {}

    public record AgentDailyLogResponse(
            boolean success
    ) {}

    public record AgentTrendResponse(
            boolean success
    ) {}

    public record AgentWeeklyPlanRequest(
            @JsonProperty("user_id") String userId,
            @JsonProperty("log_room_id") String logRoomId,
            @JsonProperty("log_room_member_id") String logRoomMemberId
    ) {}

    public record AgentWeeklyPlanResponse(
            boolean success
    ) {}

    public record AgentLogRoomDeleteResponse(
            boolean success
    ) {}

    public record AgentCharacterPromptRequest(
            String name,
            String intro,
            @JsonProperty("user_prompt") String userPrompt
    ) {}

    public record AgentCharacterPromptResponse(
            String prompt
    ) {}

    public record AgentChatRuleRequest(
            @JsonProperty("character_id") String characterId,
            String relationship
    ) {}

    public record AgentChatRuleResponse(
            @JsonProperty("chat_rule") String chatRule
    ) {}
}
