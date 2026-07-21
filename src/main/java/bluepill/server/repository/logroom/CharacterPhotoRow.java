package bluepill.server.repository.logroom;

import java.time.LocalDate;

public record CharacterPhotoRow(
        Long roomId,
        LocalDate postDate,
        Integer timeSlot,
        String imageUrl
) {}
