package bluepill.server.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CharacterCardCreateRequest {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    @Size(max = 30)
    private String description;

    @NotBlank
    private String imageUrl;

    @NotBlank
    @Size(max = 2000)
    private String prompt;

    @Size(max = 5)
    private List<@Size(max = 100) String> exampleDialogues;

    @NotNull
    private Boolean isPublic;
}
