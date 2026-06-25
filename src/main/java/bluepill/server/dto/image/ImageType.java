package bluepill.server.dto.image;

public enum ImageType {
    PROFILE("profiles"),
    CHARACTER("characters"),
    CHAT("chats");

    private final String prefix;

    ImageType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
