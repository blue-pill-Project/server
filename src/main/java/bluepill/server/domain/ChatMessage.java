package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_room_id", nullable = false, updatable = false)
    private LogRoom logRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private LogRoomMember sender;

    @Column(name = "content", nullable=false )
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="quoted_photo_id")
    private LogPhoto logPhoto;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist //DB에 저장되기 직전에 자동 실행
    void prePersist() {
        this.createdAt = Instant.now();
    }

    @Builder
    public ChatMessage(LogRoom logRoom, LogRoomMember sender,
                       String content, LogPhoto logPhoto) {
        this.logRoom = logRoom;
        this.sender = sender;
        this.content = content;
        this.logPhoto = logPhoto;
    }
}
