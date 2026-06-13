package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogRoomParticipant {
    private UUID memberPublicId;
    private String imageUrl;
}
