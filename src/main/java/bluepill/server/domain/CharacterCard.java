package bluepill.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "character_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(unique = true, updatable = false)
    private Long code;

    @Column(nullable = false)
    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "image_url")
    private String imageUrl;

    private Long useCnt;

    private Boolean isDeleted;

    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "characterCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamplePost> examplePosts = new ArrayList<>();

    @Builder
    public CharacterCard(UUID publicId, String name, Long code, String description,
                         String prompt, String imageUrl, Boolean isPublic, User creator) {
        this.publicId = publicId;
        this.name = name;
        this.code = code;
        this.version = 1;
        this.description = description;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.useCnt = 0L;
        this.isDeleted = false;
        this.isPublic = isPublic;
        this.creator = creator;
    }

    public void addExamplePost(ExamplePost post) {
        this.examplePosts.add(post);
    }

    public void incrementVersion() {
        this.version++;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void update(String name, String description, String imageUrl, String prompt, Boolean isPublic) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (prompt != null) this.prompt = prompt;
        if (isPublic != null) this.isPublic = isPublic;
    }

    public void replaceExamplePosts(List<String> contents) {
        this.examplePosts.clear();
        if (contents != null) {
            contents.forEach(content -> this.examplePosts.add(
                    ExamplePost.builder().characterCard(this).content(content).build()
            ));
        }
    }

    public void updateVisibility(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}