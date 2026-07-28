package bluepill.server.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

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

    public record AgentDailyLogRequest(
            String timeslot,
            @JsonProperty("user_id") String userId,
            @JsonProperty("log_room_id") String logRoomId,
            @JsonProperty("log_room_member_id") String logRoomMemberId,
            @JsonProperty("previous_plans") List<Object> previousPlans
    ) {}

    public record AgentDailyLogResponse(
            boolean success
    ) {}

    public record AgentTrendResponse(
            boolean success
    ) {}
}
