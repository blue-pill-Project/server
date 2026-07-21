package bluepill.server.repository.logroom;

import java.util.UUID;

public record DayLogRow(
        Integer timeSlot,
        UUID memberPublicId,
        UUID photoPublicId,
        String caption,
        String imageUrl,
        String authorType,
        String authorName,
        String authorImageUrl
) {}
