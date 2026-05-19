package bluepill.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "example_dialogues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleDialogue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "example_dialogue_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private CharacterCard characterCard;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public ExampleDialogue(CharacterCard characterCard, String content) {
        this.characterCard = characterCard;
        this.content = content;
    }
}