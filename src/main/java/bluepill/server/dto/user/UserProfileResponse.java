package bluepill.server.dto.user;

import bluepill.server.domain.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID publicId,
        String nickname,
        String profileImageUrl,
        String email,
        Long planId,
        Boolean isPublic,
        Long characterCnt,
        boolean isOwner

) {
    public static UserProfileResponse from(User user, boolean isOwner) {
        return new UserProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                user.getImageUrl(),
                user.getEmail(),
                user.getPlan() != null? user.getPlan().getId(): null,
                user.getIsPublic(),
                0L,
                isOwner
        );
    }
}
