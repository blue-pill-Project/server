package bluepill.server.repository.chat;

import bluepill.server.domain.ChatMessage;
import bluepill.server.domain.QChatMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessages(Long logRoomId, Long cursorId, int size) {
        QChatMessage cm = QChatMessage.chatMessage;

        return queryFactory
                .selectFrom(cm)
                .join(cm.sender).fetchJoin()
                .leftJoin(cm.logPhoto).fetchJoin()
                .where(
                        cm.logRoom.id.eq(logRoomId),
                        cursorCondition(cm, cursorId)
                )
                .orderBy(cm.createdAt.desc(), cm.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression cursorCondition(QChatMessage cm, Long cursor) {
        if (cursor == null) return null;

        Instant cursorCreatedAt = queryFactory
                .select(cm.createdAt)
                .from(cm)
                .where(cm.id.eq(cursor))
                .fetchOne();
        if (cursorCreatedAt == null) return null;

        return cm.createdAt.lt(cursorCreatedAt)
                .or(cm.createdAt.eq(cursorCreatedAt).and(cm.id.lt(cursor)));
    }
}
