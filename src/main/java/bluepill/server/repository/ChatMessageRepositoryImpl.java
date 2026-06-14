package bluepill.server.repository;

import bluepill.server.domain.ChatMessage;
import bluepill.server.domain.QChatMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessages(Long logRoomId, UUID cursorPublicId, int size) {
        QChatMessage cm = QChatMessage.chatMessage;

        return queryFactory
                .selectFrom(cm)
                .join(cm.sender).fetchJoin()
                .where(
                        cm.logRoom.id.eq(logRoomId),
                        cursorCondition(cm, cursorPublicId)
                )
                .orderBy(cm.createdAt.desc(), cm.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression cursorCondition(QChatMessage cm, UUID cursor) {
        if (cursor == null) return null;

        Long cursorId = queryFactory
                .select(cm.id)
                .from(cm)
                .where(cm.publicId.eq(cursor))
                .fetchOne();
        if (cursorId == null) return null;

        Instant cursorCreatedAt = queryFactory
                .select(cm.createdAt)
                .from(cm)
                .where(cm.id.eq(cursorId))
                .fetchOne();

        return cm.createdAt.lt(cursorCreatedAt)
                .or(cm.createdAt.eq(cursorCreatedAt).and(cm.id.lt(cursorId)));
    }
}
