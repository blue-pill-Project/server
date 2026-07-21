package bluepill.server.repository.character;

import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CharacterCardRepository extends JpaRepository<CharacterCard, Long>, CharacterCardRepositoryCustom {

    boolean existsByCode(Long code);

    Optional<CharacterCard> findByPublicId(UUID publicId);

    Optional<CharacterCard> findByPublicIdAndIsDeletedFalse(UUID publicId);

    @Query("SELECT c FROM CharacterCard c " +
           "LEFT JOIN FETCH c.creator " +
           "WHERE c.publicId = :publicId AND c.isDeleted = false")
    Optional<CharacterCard> findDetailByPublicId(@Param("publicId") UUID publicId);
    long countByCreatorAndIsDeletedFalse(User user);

    long countByCreatorAndIsDeletedFalseAndIsPublicTrue(User user);

    @Modifying
    @Query("UPDATE CharacterCard c SET c.isPublic = :isPublic WHERE c.creator.userId = :creatorId AND c.isDeleted = false")
    void updateIsPublicByCreator(@Param("creatorId") Long creatorId, @Param("isPublic") Boolean isPublic);
}
