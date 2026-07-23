package bluepill.server.dto.logroom;

import bluepill.server.domain.LogPhoto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogPhotoUploadResponse {

    private UUID photoPublicId;
    private UUID memberPublicId;
    private LocalDate postDate;
    private Integer timeSlot;
    private String imageUrl;
    private String caption;
    private Instant createdAt;

    public static LogPhotoUploadResponse from(LogPhoto photo, String imageUrl) {
        return new LogPhotoUploadResponse(
                photo.getPublicId(),
                photo.getMember().getPublicId(),
                photo.getPostDate(),
                photo.getTimeSlot(),
                imageUrl,
                photo.getCaption(),
                photo.getCreatedAt()
        );
    }
}
