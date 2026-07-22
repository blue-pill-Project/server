package bluepill.server.service;

import bluepill.server.client.ChatAgentClient;
import bluepill.server.domain.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final LogRoomRepository logRoomRepository;
    private final LogRoomMemberRepository logRoomMemberRepository;
    private final LogPhotoRepository logPhotoRepository;
    private final ChatAiReplyService chatAiReplyService;

    @Transactional()
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

        //답변이 오지않아도 POST 전송할 수 있도록 비동기 처리
        chatAiReplyService.generateAndSaveReply(logRoom.getId(), userId, request.content());

        return new ChatMessageResponse(
                message.getContent(),
                true,
                message.getCreatedAt(),
                quotedPhoto != null ? quotedPhoto.getImageUrl() : null
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
                        m.getCreatedAt(),
                        m.getLogPhoto() != null ? m.getLogPhoto().getImageUrl() : null
                ))
                .toList();

        Long nextCursor = hasMore && !messages.isEmpty()
                ? messages.get(messages.size() - 1).getId()
                : null;

        return new ChatMessageListResponse(content, nextCursor, hasMore);
    }

    public Long resolveRoomIdForSubscribe(UUID roomPublicId, Long userId){
        LogRoom logRoom = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        logRoomMemberRepository.findByLogRoom_IdAndUser_UserId(logRoom.getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN));

        return logRoom.getId();
    }
}
