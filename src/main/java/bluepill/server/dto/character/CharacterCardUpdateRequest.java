package bluepill.server.dto.character;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CharacterCardUpdateRequest {

    @Size(max = 30)
    private String name;

    @Size(max = 30)
    private String description;

    private String imageUrl;

    @Size(max = 2000)
    private String prompt;

    @Size(max = 5)
    private List<@Size(max = 100) String> exampleDialogues;

    private Boolean isPublic;

    public boolean hasContentChanges() {
        return name != null
                || description != null
                || imageUrl != null
                || prompt != null
                || exampleDialogues != null;
    }
}
