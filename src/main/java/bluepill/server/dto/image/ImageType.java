package bluepill.server.dto.image;

public enum ImageType {
    PROFILE("users"),
    CHARACTER("characters"),
    LOG("logs");

    private final String prefix;

    ImageType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
