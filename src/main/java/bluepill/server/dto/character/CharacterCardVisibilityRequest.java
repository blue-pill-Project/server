package bluepill.server.dto.character;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CharacterCardVisibilityRequest {

    @NotNull
    private Boolean isPublic;
}
