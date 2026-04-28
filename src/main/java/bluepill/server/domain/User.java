package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "account_name", length = 20)
    private String accountName;

    @Column(name = "nickname", length = 15)
    private String nickname;

    @Column(name = "email", length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "delete_reason", columnDefinition = "TEXT")
    private String deleteReason;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public enum Role {
        GUEST, USER
    }
}
