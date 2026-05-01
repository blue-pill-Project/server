package bluepill.server.dto.character;

public enum CharacterSortType {
    LATEST,
    POPULAR;

    public static CharacterSortType from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        try {
            return CharacterSortType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LATEST;
        }
    }
}
