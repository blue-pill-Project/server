package bluepill.server.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatAgentRequest(
        @JsonProperty("log_room_id") String logRoomId,
        @JsonProperty("character_id") String characterId,
        @JsonProperty("user_id") String userId,
        String content
) {
}
