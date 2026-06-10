package bluepill.server.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostListResponse {
    private List<PostListItem> content;
    private UUID nextCursor;
    private Boolean hasNext;
}
