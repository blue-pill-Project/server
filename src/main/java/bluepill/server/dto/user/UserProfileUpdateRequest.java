package bluepill.server.dto.user;

public record UserProfileUpdateRequest(
    String nickname,
    String profileImageUrl
) {
}
