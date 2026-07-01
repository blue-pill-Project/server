package bluepill.server.dto.user;

import bluepill.server.domain.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID publicId,
        String nickname,
        String profileImageUrl,
        String email,
        String planName,
        Boolean isPublic,
        Long characterCount,
        Long postCount,
        boolean isOwner
) {
    public static UserProfileResponse from(User user, boolean isOwner, Long characterCount, Long postCount, String profileImageUrl) {
        return new UserProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                profileImageUrl,
                user.getEmail(),
                isOwner && user.getPlan() != null ? user.getPlan().getPlanName() : null,
                user.getIsPublic(),
                characterCount,
                postCount,
                isOwner
        );
    }

    public static UserProfileResponse from(User user, boolean isOwner) {
        return new UserProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                user.getImageUrl(),
                user.getEmail(),
                isOwner && user.getPlan() != null ? user.getPlan().getPlanName() : null,
                user.getIsPublic(),
                0L,
                0L,
                isOwner
        );
    }
}
