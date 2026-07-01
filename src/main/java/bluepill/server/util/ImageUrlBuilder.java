package bluepill.server.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlBuilder {
    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;

    public String buildUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        return publicDomain + "/" + imageKey;
    }
}
