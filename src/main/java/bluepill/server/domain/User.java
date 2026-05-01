package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_reason")
    private String deletedReason;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Provider Enum
    public enum Provider {
        GOOGLE, DISCORD
    }

    // 신규 유저 생성
    public static User createNewUser(String providerId, Provider provider, String email, String imageUrl) {
        return User.builder()
                .publicId(UUID.randomUUID())
                .providerId(providerId)
                .provider(provider)
                .email(email)
                .imageUrl(imageUrl)
                .isDeleted(false)
                .isPublic(true)
                .build();
    }
}
