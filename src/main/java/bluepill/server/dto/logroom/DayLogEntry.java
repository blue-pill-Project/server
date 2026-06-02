package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class DayLogEntry {
    private UUID memberPublicId;
    private UUID photoPublicId;
    private String caption;
    private String imageUrl;
    private String authorType;
    private String authorName;
    private String authorImageUrl;
}
