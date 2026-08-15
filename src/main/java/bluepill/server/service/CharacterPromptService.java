package bluepill.server.service;

import bluepill.server.client.AgentClient;
import bluepill.server.client.AgentClient.AgentCharacterPromptRequest;
import bluepill.server.client.AgentClient.AgentCharacterPromptResponse;
import bluepill.server.domain.User;
import bluepill.server.dto.character.CharacterPromptAutoCompleteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterPromptService {

    private final UserService userService;
    private final UserDailyLimitService userDailyLimitService;
    private final AgentClient agentClient;

    // @Transactional 을 붙이지 않는다:
    // 일일 횟수 증가(increasePromptAutoCompleteCount)가 먼저 자체 커밋되어야
    // agent 호출이 실패해도 시도 횟수가 유지된다(attempt 기준).
    public String autoComplete(Long userId, CharacterPromptAutoCompleteRequest request) {
        User user = userService.findById(userId);

        // 일일 제한 체크 + 카운트 증가 (초과 시 429)
        userDailyLimitService.increasePromptAutoCompleteCount(user);

        String userPrompt = request.getPrompt() != null ? request.getPrompt() : "";

        AgentCharacterPromptResponse response = agentClient.completeCharacterPrompt(
                new AgentCharacterPromptRequest(
                        request.getName(),
                        request.getIntro(),
                        userPrompt
                )
        );

        return response.prompt();
    }
}
