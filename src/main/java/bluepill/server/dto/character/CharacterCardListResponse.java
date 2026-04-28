package bluepill.server.dto.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CharacterCardListResponse {

    private List<CharacterCardListItem> content;
    private UUID nextCursor;   // 마지막 페이지면 null
    private boolean hasNext;
}
