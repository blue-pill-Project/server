package bluepill.server.dto.logroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogPhotoUploadRequest {

    @NotBlank
    private String imageUrl;

    @Size(max = 30)
    private String caption;
}
