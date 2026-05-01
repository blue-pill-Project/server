package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_daily_limits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyLimit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "character_create_count", nullable = false)
    private Integer characterCreateCount;

    @Column(name = "image_generate_count", nullable = false)
    private Integer imageGenerateCount;

    @Builder
    public UserDailyLimit(User user) {
        this.user = user;
        this.characterCreateCount = 0;
        this.imageGenerateCount = 0;
    }

    public void increaseCharacterCreateCount() {
        this.characterCreateCount++;
    }

    public void increaseImageGenerateCount() {
        this.imageGenerateCount++;
    }
}
