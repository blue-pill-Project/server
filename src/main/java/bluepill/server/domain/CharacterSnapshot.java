package bluepill.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "character_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_character_snapshot_character_version",
                columnNames = {"character_id", "version"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "example_dialogues", columnDefinition = "jsonb")
    private List<String> exampleDialogues;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Builder
    public CharacterSnapshot(Long characterId, Integer version,
                             String name, String description, String prompt,
                             String imageUrl, List<String> exampleDialogues) {
        this.characterId = characterId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.exampleDialogues = exampleDialogues;
        this.createdAt = Instant.now();
    }
}
