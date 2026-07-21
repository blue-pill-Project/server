package bluepill.server.repository.post;

import java.util.List;
import java.util.UUID;

public interface PostRepositoryCustom {

    List<PostPageRow> findPostsPage(Long roomId, UUID cursorPublicId, int size);

    List<PostPageRow> findAllPostsPage(UUID cursorPublicId, int size);
    List<PostPhotoRow> findPhotosByPostIds(List<Long> postIds);
}
