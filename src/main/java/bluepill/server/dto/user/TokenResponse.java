package bluepill.server.dto.user;

public record TokenResponse (
        String accessToken,
        boolean isNewUser
){}
