package bluepill.server.dto.image;

public record ImagePresignedUrlRequest(
        String filename,
        String contentType,
        ImageType imageType
) {

}
