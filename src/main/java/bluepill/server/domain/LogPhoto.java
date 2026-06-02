package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "log_photos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_log_photo_member_date_slot",
                columnNames = {"log_room_member_id", "post_date", "time_slot"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_photo_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_room_member_id", nullable = false, updatable = false)
    private LogRoomMember member;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Column(name = "time_slot", nullable = false)
    private Integer timeSlot;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(length = 30)
    private String caption;

    @Builder
    public LogPhoto(UUID publicId, LogRoomMember member, LocalDate postDate,
                    Integer timeSlot, String imageUrl, String caption) {
        this.publicId = publicId;
        this.member = member;
        this.postDate = postDate;
        this.timeSlot = timeSlot;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }
}
