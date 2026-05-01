package bluepill.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "character_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

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
    @Column(name = "example_posts", columnDefinition = "jsonb")
    private List<String> examplePosts;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Builder
    public CharacterSnapshot(UUID sessionId, Long characterId, Integer version,
                             String name, String description, String prompt,
                             String imageUrl, List<String> examplePosts,
                             Boolean isMain, Integer displayOrder) {
        this.sessionId = sessionId;
        this.characterId = characterId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.examplePosts = examplePosts;
        this.isMain = isMain;
        this.displayOrder = displayOrder;
        this.createdAt = Instant.now();
    }
}
