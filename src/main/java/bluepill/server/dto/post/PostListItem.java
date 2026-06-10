package bluepill.server.dto.post;

import bluepill.server.dto.logroom.DayLogEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostListItem {
    private UUID publicId;
    private LocalDate postDate;
    private Integer timeSlot;
    private PostSharer sharer;
    private Boolean isMine;
    private Instant createdAt;
    private List<DayLogEntry> photos;
}
