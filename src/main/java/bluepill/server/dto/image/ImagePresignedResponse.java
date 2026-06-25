package bluepill.server.dto.image;

public record ImagePresignedResponse(
        String uploadUrl,
        String key
) {
}
