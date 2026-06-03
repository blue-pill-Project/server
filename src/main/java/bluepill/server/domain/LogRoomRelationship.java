package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_room_relationships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogRoomRelationship extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_room_relationship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_room_id", nullable = false, updatable = false)
    private LogRoom logRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_a_id", nullable = false, updatable = false)
    private LogRoomMember memberA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_b_id", nullable = false, updatable = false)
    private LogRoomMember memberB;

    @Column(nullable = false, length = 24)
    private String label;

    @Builder
    public LogRoomRelationship(LogRoom logRoom, LogRoomMember memberA, LogRoomMember memberB, String label) {
        this.logRoom = logRoom;
        this.memberA = memberA;
        this.memberB = memberB;
        this.label = label;
    }
}
