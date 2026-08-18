package bluepill.server.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatAgentRequest(
        @JsonProperty("log_room_id") String logRoomId,
        @JsonProperty("log_room_member_id") String logRoomMemberId,
        @JsonProperty("user_id") String userId,
        String content
) {
}
