package bluepill.server.repository.logroom;

import bluepill.server.domain.QCharacterSnapshot;
import bluepill.server.domain.QLogPhoto;
import bluepill.server.domain.QLogRoom;
import bluepill.server.domain.QLogRoomMember;
import bluepill.server.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class LogRoomRepositoryImpl implements LogRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LogRoomPageRow> findMyLogRoomsPage(Long viewerId, UUID cursor, int size) {
        QLogRoom r = QLogRoom.logRoom;
        QUser u = QUser.user;
        QLogRoomMember m = QLogRoomMember.logRoomMember;

        return queryFactory
                .select(Projections.constructor(LogRoomPageRow.class,
                        r.id, r.publicId, r.name, r.isPublic, r.createdAt,
                        u.userId, u.publicId, u.nickname))
                .from(r)
                .join(r.createdBy, u)
                .where(
                        // 요청자가 멤버인 방만
                        JPAExpressions.selectOne()
                                .from(m)
                                .where(m.logRoom.eq(r).and(m.user.userId.eq(viewerId)))
                                .exists(),
                        cursorCondition(r, cursor)
                )
                .orderBy(r.createdAt.desc(), r.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    @Override
    public List<MemberImageRow> findMemberImagesByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) return List.of();

        QLogRoomMember m = QLogRoomMember.logRoomMember;
        QUser u = QUser.user;
        QCharacterSnapshot s = QCharacterSnapshot.characterSnapshot;

        return queryFactory
                .select(Projections.constructor(MemberImageRow.class,
                        m.logRoom.id,
                        m.publicId,
                        s.name.coalesce(u.nickname),         // 캐릭터면 스냅샷 이름, 사람이면 닉네임
                        u.userId,                            // 캐릭터 멤버면 null
                        s.imageUrl.coalesce(u.imageUrl)))   // 캐릭터면 스냅샷, 사람이면 유저 프로필
                .from(m)
                .leftJoin(m.user, u)
                .leftJoin(m.snapshot, s)
                .where(m.logRoom.id.in(roomIds))
                .orderBy(m.logRoom.id.asc(), m.createdAt.asc())
                .fetch();
    }

    @Override
    public List<CharacterPhotoRow> findCharacterPhotosByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) return List.of();

        QLogPhoto photo = QLogPhoto.logPhoto;
        QLogRoomMember m = QLogRoomMember.logRoomMember;

        return queryFactory
                .select(Projections.constructor(CharacterPhotoRow.class,
                        m.logRoom.id,
                        photo.postDate,
                        photo.timeSlot,
                        photo.imageUrl))
                .from(photo)
                .join(photo.member, m)
                .where(
                        m.logRoom.id.in(roomIds),
                        m.snapshot.isNotNull()       // 캐릭터 멤버만
                )
                .orderBy(m.logRoom.id.asc(), photo.postDate.desc(), photo.timeSlot.desc())
                .fetch();
    }

    private BooleanExpression cursorCondition(QLogRoom r, UUID cursor) {
        if (cursor == null) return null;

        Long cursorId = queryFactory.select(r.id).from(r).where(r.publicId.eq(cursor)).fetchOne();
        if (cursorId == null) return null;

        Instant cursorCreatedAt = queryFactory.select(r.createdAt).from(r).where(r.id.eq(cursorId)).fetchOne();
        return r.createdAt.lt(cursorCreatedAt)
                .or(r.createdAt.eq(cursorCreatedAt).and(r.id.lt(cursorId)));
    }
}
