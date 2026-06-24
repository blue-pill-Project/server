package bluepill.server.repository;

import java.time.LocalDate;

public record CharacterPhotoRow(
        Long roomId,
        LocalDate postDate,
        Integer timeSlot,
        String imageUrl
) {}
