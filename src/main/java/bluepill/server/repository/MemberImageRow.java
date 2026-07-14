package bluepill.server.repository;

import java.util.UUID;

public record MemberImageRow(
        Long roomId,
        UUID memberPublicId,
        String memberName,
        Long memberUserId,
        String imageUrl
) {}
