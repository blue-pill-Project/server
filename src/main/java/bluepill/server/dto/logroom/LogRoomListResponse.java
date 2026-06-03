package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogRoomListResponse {
    private List<LogRoomListItem> content;
    private UUID nextCursor;
    private Boolean hasNext;
}
