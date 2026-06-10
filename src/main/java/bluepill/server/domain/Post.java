package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "posts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_room_date_slot",
                columnNames = {"log_room_id", "post_date", "time_slot"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_room_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LogRoom logRoom;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Column(name = "time_slot", nullable = false)
    private Integer timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @Builder
    public Post(UUID publicId, LogRoom logRoom, LocalDate postDate, Integer timeSlot, User createdBy) {
        this.publicId = publicId;
        this.logRoom = logRoom;
        this.postDate = postDate;
        this.timeSlot = timeSlot;
        this.createdBy = createdBy;
    }
}
