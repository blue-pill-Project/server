package bluepill.server.repository.post;

import java.util.UUID;

public record PostPhotoRow(
        Long postId,
        UUID memberPublicId,
        UUID photoPublicId,
        String caption,
        String imageUrl,
        String authorType,
        String authorName,
        String authorImageUrl
) {}
