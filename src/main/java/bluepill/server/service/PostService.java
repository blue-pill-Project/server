package bluepill.server.service;

import bluepill.server.domain.LogRoom;
import bluepill.server.domain.Post;
import bluepill.server.domain.User;
import bluepill.server.dto.post.PostShareRequest;
import bluepill.server.dto.post.PostShareResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.LogRoomMemberRepository;
import bluepill.server.repository.LogRoomRepository;
import bluepill.server.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final LogRoomRepository logRoomRepository;
    private final LogRoomMemberRepository logRoomMemberRepository;
    private final UserService userService;

    @Transactional
    public PostShareResponse sharePost(UUID roomPublicId, PostShareRequest request, Long viewerId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크
        boolean isMember = logRoomMemberRepository.existsByLogRoom_IdAndUser_UserId(room.getId(), viewerId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }

        // 공개 로그방 검증
        if (!room.getIsPublic()) {
            throw new BusinessException(ErrorCode.LOG_ROOM_PRIVATE);
        }

        // 공유자 조회
        User sharer = userService.findById(viewerId);

        // Post INSERT (unique 위반 시 409)
        Post post;
        try {
            post = postRepository.save(Post.builder()
                    .publicId(UUID.randomUUID())
                    .logRoom(room)
                    .postDate(request.getPostDate())
                    .timeSlot(request.getTimeSlot())
                    .createdBy(sharer)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.POST_ALREADY_SHARED);
        }

        return PostShareResponse.from(post);
    }
}
