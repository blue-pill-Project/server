package bluepill.server.dto.user;

import java.util.UUID;

public record UserProfileResponse(
        UUID publicId,
        String nickname,
        String profileImageUrl,
        String email,
        Long planId,
        Boolean isPublic,
        Long characterCnt,
        Long postCnt,
        boolean isOwner

) {
}
