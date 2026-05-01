package bluepill.server.dto.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserCharacterCardListResponse {

    private List<UserCharacterCardListItem> content;
    private UUID nextCursor;
    private boolean hasNext;
}
