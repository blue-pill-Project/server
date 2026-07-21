package bluepill.server.repository.logroom;

import java.util.List;
import java.util.UUID;

public interface LogRoomRepositoryCustom {

    List<LogRoomPageRow> findMyLogRoomsPage(Long viewerId, UUID cursor, int size);

    List<MemberImageRow> findMemberImagesByRoomIds(List<Long> roomIds);

    List<CharacterPhotoRow> findCharacterPhotosByRoomIds(List<Long> roomIds);
}
