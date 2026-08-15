package bluepill.server.dto.character;

import lombok.Getter;

@Getter
public class CharacterPromptAutoCompleteResponse {

    private final String prompt;

    public CharacterPromptAutoCompleteResponse(String prompt) {
        this.prompt = prompt;
    }
}
