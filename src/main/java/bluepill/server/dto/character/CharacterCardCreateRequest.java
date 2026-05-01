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

    private List<@Size(max = 1000) String> examplePosts;

    @NotNull
    private Boolean isPublic;
}
