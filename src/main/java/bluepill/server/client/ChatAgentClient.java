package bluepill.server.client;

import bluepill.server.dto.client.ChatAgentRequest;
import bluepill.server.dto.client.ChatAgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAgentClient {

    private final RestClient chatAgentRestClient;

    public String generateReply(String logRoomId, String characterId, String userId, String content){
        ChatAgentRequest request = new ChatAgentRequest(logRoomId, characterId, userId, content);
        ChatAgentResponse response = chatAgentRestClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatAgentResponse.class);
        return response!=null ? response.reply() : null;
    }
}
