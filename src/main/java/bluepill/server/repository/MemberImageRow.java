package bluepill.server.repository;

import java.util.UUID;

public record MemberImageRow(
        Long roomId,
        UUID memberPublicId,
        Long memberUserId,
        String imageUrl
) {}
