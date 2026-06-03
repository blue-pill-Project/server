package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "log_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogRoomMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_room_member_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_room_id", nullable = false, updatable = false)
    private LogRoom logRoom;

    // 사람 참여자 (캐릭터면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 캐릭터 참여자 (사람이면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private CharacterSnapshot snapshot;

    @Builder
    public LogRoomMember(UUID publicId, LogRoom logRoom, User user, CharacterSnapshot snapshot) {
        this.publicId = publicId;
        this.logRoom = logRoom;
        this.user = user;
        this.snapshot = snapshot;
    }

    public void updateSnapshot(CharacterSnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
