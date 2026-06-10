package bluepill.server.dto.post;

import bluepill.server.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostShareResponse {

    private UUID publicId;
    private UUID logRoomPublicId;
    private LocalDate postDate;
    private Integer timeSlot;
    private Instant createdAt;

    public static PostShareResponse from(Post post) {
        return new PostShareResponse(
                post.getPublicId(),
                post.getLogRoom().getPublicId(),
                post.getPostDate(),
                post.getTimeSlot(),
                post.getCreatedAt()
        );
    }
}
