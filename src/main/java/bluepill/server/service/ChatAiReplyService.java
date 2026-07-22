package bluepill.server.service;

import bluepill.server.client.ChatAgentClient;
import bluepill.server.domain.CharacterSnapshot;
import bluepill.server.domain.ChatMessage;
import bluepill.server.domain.LogRoom;
import bluepill.server.dto.chat.ChatMessageItem;
import bluepill.server.repository.ChatMessageRepository;
import bluepill.server.repository.ChatSseEmitterRepository;
import bluepill.server.repository.LogRoomMemberRepository;
import bluepill.server.repository.LogRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAiReplyService {

    private final LogRoomMemberRepository logRoomMemberRepository;
    private  final LogRoomRepository logRoomRepository;
    private final ChatAgentClient chatAgentClient;
    private  final ChatMessageRepository chatMessageRepository;
    private  final ChatSseEmitterRepository chatSseEmitterRepository;

    @Async("chatAiExecutor")
    @Transactional
    public void generateAndSaveReply(Long logRoomId, Long userId, String content){
        logRoomMemberRepository.findByLogRoom_IdAndSnapshotIsNotNull(logRoomId)
                .ifPresent(characterMember -> {
                    LogRoom logRoom = logRoomRepository.findById(logRoomId).orElseThrow();
                    CharacterSnapshot snapshot = characterMember.getSnapshot();
                    try {
                        String aiReply = chatAgentClient.generateReply(
                                logRoomId.toString(),
                                snapshot.getCharacterId().toString(),
                                userId.toString(),
                                content
                        );
                        if (aiReply != null) {
                            ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                                    .logRoom(logRoom)
                                    .sender(characterMember)
                                    .content(aiReply)
                                    .build());

                            //저장완료 즉시 구독중인 클라이언트에 push
                            chatSseEmitterRepository.sendToRoom(logRoomId,
                                    new ChatMessageItem(saved.getContent(), false, saved.getCreatedAt(), null));
                        }
                    }catch (Exception e){
                        log.error("chat-agent 호출 실패 (roomId={})", logRoomId, e);
                    }
                });
    }
}
