package bluepill.server.dto.chat;

import java.util.UUID;

public record ChatMessageRequest(String content,
                                 UUID  quotedPhotoPublicId) {
}
