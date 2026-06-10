package bluepill.server.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostSharer {
    private UUID publicId;
    private String nickname;
    private String profileImageUrl;
}
