package bluepill.server.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CharacterPromptAutoCompleteRequest {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String intro;

    // 사용자가 쓴 캐릭터 프롬프트 초안 (없거나 미완성일 수 있음)
    @Size(max = 2000)
    private String prompt;
}
