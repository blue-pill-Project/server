package bluepill.server.service;

import bluepill.server.domain.LogRoom;
import bluepill.server.domain.Post;
import bluepill.server.domain.User;
import bluepill.server.dto.logroom.DayLogEntry;
import bluepill.server.dto.post.PostListItem;
import bluepill.server.dto.post.PostListResponse;
import bluepill.server.dto.post.PostShareRequest;
import bluepill.server.dto.post.PostShareResponse;
import bluepill.server.dto.post.PostSharer;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.logroom.LogRoomMemberRepository;
import bluepill.server.repository.logroom.LogRoomRepository;
import bluepill.server.repository.post.PostPageRow;
import bluepill.server.repository.post.PostPhotoRow;
import bluepill.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public PostListResponse getPostsInRoom(UUID roomPublicId, UUID cursor, int size, Long viewerId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크
        boolean isMember = logRoomMemberRepository.existsByLogRoom_IdAndUser_UserId(room.getId(), viewerId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }

        // 쿼리1: 게시물 페이지 (+공유자)
        List<PostPageRow> page = postRepository.findPostsPage(room.getId(), cursor, size);

        boolean hasNext = page.size() > size;
        if (hasNext) {
            page = page.subList(0, size);
        }

        // 쿼리2: 그 게시물들의 사진 일괄 조회
        List<Long> postIds = page.stream().map(PostPageRow::postId).toList();
        List<PostPhotoRow> photoRows = postRepository.findPhotosByPostIds(postIds);

        // 게시물별 사진 그룹화 (post.id ASC, member 입장 순으로 이미 정렬되어 있음)
        Map<Long, List<DayLogEntry>> photosByPost = new LinkedHashMap<>();
        for (PostPhotoRow row : photoRows) {
            photosByPost.computeIfAbsent(row.postId(), k -> new ArrayList<>())
                    .add(new DayLogEntry(
                            row.memberPublicId(),
                            row.photoPublicId(),
                            row.caption(),
                            row.imageUrl(),
                            row.authorType(),
                            row.authorName(),
                            row.authorImageUrl()
                    ));
        }

        // PostListItem 조립
        List<PostListItem> content = page.stream()
                .map(r -> new PostListItem(
                        r.publicId(),
                        r.postDate(),
                        r.timeSlot(),
                        new PostSharer(r.sharerPublicId(), r.sharerNickname(), r.sharerProfileImageUrl()),
                        r.sharerUserId().equals(viewerId),
                        r.createdAt(),
                        photosByPost.getOrDefault(r.postId(), List.of())))
                .toList();

        UUID nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).getPublicId()
                : null;

        long total = postRepository.countByLogRoom(room);

        return new PostListResponse(content, nextCursor, hasNext, total);
    }

    public PostListResponse getPosts(UUID cursor, int size, Long viewerId) {
        List<PostPageRow> page = postRepository.findAllPostsPage(cursor, size);

        boolean hasNext = page.size() > size;
        if (hasNext) {
            page = page.subList(0, size);
        }

        List<Long> postIds = page.stream().map(PostPageRow::postId).toList();
        List<PostPhotoRow> photoRows = postIds.isEmpty()
                ? List.of()
                : postRepository.findPhotosByPostIds(postIds);

        // 게시물별 사진 그룹화
        Map<Long, List<DayLogEntry>> photosByPost = new LinkedHashMap<>();
        for (PostPhotoRow row : photoRows) {
            photosByPost.computeIfAbsent(row.postId(), k -> new ArrayList<>())
                    .add(new DayLogEntry(
                            row.memberPublicId(),
                            row.photoPublicId(),
                            row.caption(),
                            row.imageUrl(),
                            row.authorType(),
                            row.authorName(),
                            row.authorImageUrl()
                    ));
        }

        List<PostListItem> content = page.stream()
                .map(row -> new PostListItem(
                        row.publicId(),
                        row.postDate(),
                        row.timeSlot(),
                        new PostSharer(row.sharerPublicId(), row.sharerNickname(), row.sharerProfileImageUrl()),
                        viewerId != null && row.sharerUserId().equals(viewerId),
                        row.createdAt(),
                        photosByPost.getOrDefault(row.postId(), List.of())))
                .toList();

        UUID nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).getPublicId()
                : null;

        long total = postRepository.count();

        return new PostListResponse(content, nextCursor, hasNext, total);

    }

    public void deletePost(UUID publicId, Long viewerId) {
        Post post = postRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if(!post.getCreatedBy().getUserId().equals(viewerId)){
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }

        postRepository.delete(post);
    }
}
