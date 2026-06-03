package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "log_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_room_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(nullable = false, length = 12)
    private String name;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @Builder
    public LogRoom(UUID publicId, String name, Boolean isPublic, User createdBy) {
        this.publicId = publicId;
        this.name = name;
        this.isPublic = isPublic;
        this.createdBy = createdBy;
    }
}
