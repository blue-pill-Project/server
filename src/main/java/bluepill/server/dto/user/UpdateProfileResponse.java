package bluepill.server.dto.user;

import bluepill.server.domain.User;

import java.util.UUID;

public record UpdateProfileResponse(
        UUID publicId,
        String nickname,
        String profileImageUrl,
        String email,
        String planName,
        Boolean isPublic
) {
    public static UpdateProfileResponse from(User user, String profileImageUrl) {
        return new UpdateProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                profileImageUrl,
                user.getEmail(),
                user.getPlan() != null ? user.getPlan().getPlanName() : null,
                user.getIsPublic()
        );
    }
}
