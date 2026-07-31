package bluepill.server.repository.logroom;

import bluepill.server.domain.LogPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogPhotoRepository extends JpaRepository<LogPhoto, Long>, LogPhotoRepositoryCustom {

    Optional<LogPhoto> findByPublicId(UUID publicId);

    // 방 삭제용: 방에 속한 사진들의 R2 key 수집 (삭제 전 호출)
    @Query("select p.imageUrl from LogPhoto p where p.member.logRoom.id = :roomId")
    List<String> findImageUrlsByRoomId(@Param("roomId") Long roomId);

    // 방 삭제용: 방 멤버들의 사진 일괄 삭제
    @Modifying
    @Query("delete from LogPhoto p where p.member.id in (select m.id from LogRoomMember m where m.logRoom.id = :roomId)")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
