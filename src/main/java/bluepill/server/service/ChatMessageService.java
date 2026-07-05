package bluepill.server.service;

import bluepill.server.domain.ChatMessage;
import bluepill.server.domain.LogPhoto;
import bluepill.server.domain.LogRoom;
import bluepill.server.domain.LogRoomMember;
import bluepill.server.dto.chat.ChatMessageListResponse;
import bluepill.server.dto.chat.ChatMessageItem;
import bluepill.server.dto.chat.ChatMessageRequest;
import bluepill.server.dto.chat.ChatMessageResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.ChatMessageRepository;
import bluepill.server.repository.LogPhotoRepository;
import bluepill.server.repository.LogRoomMemberRepository;
import bluepill.server.repository.LogRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final LogRoomRepository logRoomRepository;
    private final LogRoomMemberRepository logRoomMemberRepository;
    private final LogPhotoRepository logPhotoRepository;

    public ChatMessageResponse send(UUID roomPublicId, ChatMessageRequest request, Long userId) {
        LogRoom logRoom = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        LogRoomMember sender = logRoomMemberRepository
                .findByLogRoom_IdAndUser_UserId(logRoom.getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN));

        LogPhoto quotedPhoto = null;
        if (request.quotedPhotoPublicId() != null) {
            quotedPhoto = logPhotoRepository.findByPublicId(request.quotedPhotoPublicId())
                    .orElse(null);
        }

        ChatMessage message = ChatMessage.builder()
                .logRoom(logRoom)
                .sender(sender)
                .content(request.content())
                .logPhoto(quotedPhoto)
                .build();

        chatMessageRepository.save(message);

        return new ChatMessageResponse(
                message.getContent(),
                true,
                message.getCreatedAt()
        );
    }

    public ChatMessageListResponse getMessages(UUID roomPublicId, Long cursor, int size, Long userId) {

        LogRoom logRoom = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크
        LogRoomMember member = logRoomMemberRepository
                .findByLogRoom_IdAndUser_UserId(logRoom.getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN));

        List<ChatMessage> messages = chatMessageRepository
                .findMessages(logRoom.getId(), cursor, size);

        boolean hasMore = messages.size() > size;
        if (hasMore) {
            messages = messages.subList(0, size);
        }

        List<ChatMessageItem> content = messages.stream()
                .map(m -> new ChatMessageItem(
                        m.getContent(),
                        m.getSender().getId().equals(member.getId()),
                        m.getCreatedAt()
                ))
                .toList();

        Long nextCursor = hasMore && !messages.isEmpty()
                ? messages.get(messages.size() - 1).getId()
                : null;

        return new ChatMessageListResponse(content, nextCursor, hasMore);
    }
}
