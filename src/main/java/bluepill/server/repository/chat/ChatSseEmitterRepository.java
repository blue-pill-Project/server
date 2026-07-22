package bluepill.server.repository.chat;

import bluepill.server.dto.chat.ChatMessageItem;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatSseEmitterRepository {

    //채팅방 구독중 emitter들
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long logRoomId){
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); //1시간 타임아웃

        emitters.computeIfAbsent(logRoomId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable remove = () -> {
            List<SseEmitter> list = emitters.get(logRoomId);
            if(list != null) list.remove(emitter);
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        return emitter;
    }

    public void sendToRoom(Long logRoomId, ChatMessageItem message){
        List<SseEmitter> list = emitters.get(logRoomId);

        if(list == null) return;

        for(SseEmitter emitter: list) {
            try{
                emitter.send(SseEmitter.event().name("chat-message").data(message));
            } catch (IOException e) {
                emitter.complete();
                list.remove(emitter);
            }
        }
    }
}
