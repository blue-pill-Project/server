package bluepill.server.repository;

import bluepill.server.dto.character.CharacterCardListItem;
import bluepill.server.dto.character.CharacterSortType;
import bluepill.server.dto.character.UserCharacterCardListItem;

import java.util.List;
import java.util.UUID;

public interface CharacterCardRepositoryCustom {

    List<CharacterCardListItem> findLibrary(
            String keyword,
            CharacterSortType sort,
            UUID cursor,
            int size
    );

    List<UserCharacterCardListItem> findByCreator(
            Long creatorId,
            boolean includePrivate,
            UUID cursor,
            int size
    );
}
