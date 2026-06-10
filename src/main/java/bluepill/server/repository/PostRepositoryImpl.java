package bluepill.server.repository;

import bluepill.server.domain.QCharacterSnapshot;
import bluepill.server.domain.QLogPhoto;
import bluepill.server.domain.QLogRoomMember;
import bluepill.server.domain.QPost;
import bluepill.server.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostPageRow> findPostsPage(Long roomId, UUID cursorPublicId, int size) {
        QPost p = QPost.post;
        QUser u = QUser.user;

        return queryFactory
                .select(Projections.constructor(PostPageRow.class,
                        p.id, p.publicId, p.postDate, p.timeSlot, p.createdAt,
                        u.userId, u.publicId, u.nickname, u.imageUrl))
                .from(p)
                .join(p.createdBy, u)
                .where(
                        p.logRoom.id.eq(roomId),
                        cursorCondition(p, cursorPublicId)
                )
                .orderBy(p.createdAt.desc(), p.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    @Override
    public List<PostPhotoRow> findPhotosByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return List.of();

        QPost p = QPost.post;
        QLogPhoto photo = QLogPhoto.logPhoto;
        QLogRoomMember m = QLogRoomMember.logRoomMember;
        QUser u = QUser.user;
        QCharacterSnapshot s = QCharacterSnapshot.characterSnapshot;

        return queryFactory
                .select(Projections.constructor(PostPhotoRow.class,
                        p.id,
                        m.publicId,
                        photo.publicId,
                        photo.caption,
                        photo.imageUrl,
                        new CaseBuilder()
                                .when(m.user.isNotNull()).then("USER")
                                .otherwise("CHARACTER"),
                        s.name.coalesce(u.nickname),       // 캐릭터면 스냅샷 이름, 사람이면 닉네임
                        s.imageUrl.coalesce(u.imageUrl)))  // 캐릭터면 스냅샷 이미지, 사람이면 프로필
                .from(p)
                .innerJoin(m).on(m.logRoom.id.eq(p.logRoom.id))   // 같은 방의 멤버
                .innerJoin(photo).on(                              // 그 멤버가 해당 (날짜, 시간대)에 올린 사진
                        photo.member.eq(m)
                                .and(photo.postDate.eq(p.postDate))
                                .and(photo.timeSlot.eq(p.timeSlot))
                )
                .leftJoin(m.user, u)
                .leftJoin(m.snapshot, s)
                .where(p.id.in(postIds))
                .orderBy(p.id.asc(), m.createdAt.asc())
                .fetch();
    }

    private BooleanExpression cursorCondition(QPost p, UUID cursor) {
        if (cursor == null) return null;

        Long cursorId = queryFactory.select(p.id).from(p).where(p.publicId.eq(cursor)).fetchOne();
        if (cursorId == null) return null;

        Instant cursorCreatedAt = queryFactory.select(p.createdAt).from(p).where(p.id.eq(cursorId)).fetchOne();
        return p.createdAt.lt(cursorCreatedAt)
                .or(p.createdAt.eq(cursorCreatedAt).and(p.id.lt(cursorId)));
    }
}
