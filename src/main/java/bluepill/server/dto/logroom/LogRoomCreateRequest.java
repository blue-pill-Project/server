package bluepill.server.dto.logroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class LogRoomCreateRequest {

    @NotBlank
    @Size(max = 12)
    private String name;

    @NotNull
    @Size(min = 1, max = 1, message = "현재 로그방에는 캐릭터를 1명만 추가할 수 있습니다.")
    private List<UUID> characterCardPublicIds;

    @NotBlank
    @Size(max = 24)
    private String relationship;

    @NotNull
    private Boolean isPublic;
}
