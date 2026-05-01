package bluepill.server.repository;

import bluepill.server.domain.QCharacterCard;
import bluepill.server.domain.QUser;
import bluepill.server.dto.character.CharacterCardListItem;
import bluepill.server.dto.character.CharacterSortType;
import bluepill.server.dto.character.UserCharacterCardListItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CharacterCardRepositoryImpl implements CharacterCardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CharacterCardListItem> findLibrary(
            String keyword,
            CharacterSortType sort,
            UUID cursor,
            int size
    ) {
        QCharacterCard c = QCharacterCard.characterCard;
        QUser u = QUser.user;

        return queryFactory
                .select(Projections.constructor(CharacterCardListItem.class,
                        c.publicId,
                        c.name,
                        c.code,
                        c.version,
                        c.description,
                        c.imageUrl,
                        u.publicId,
                        u.nickname,
                        c.useCnt,
                        c.createdAt,
                        c.updatedAt))
                .from(c)
                .leftJoin(c.creator, u)
                .where(
                        c.isDeleted.isFalse(),
                        c.isPublic.isTrue(),
                        keywordContains(c, u, keyword),
                        cursorCondition(c, sort, cursor)
                )
                .orderBy(orderBy(c, sort))
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression keywordContains(QCharacterCard c, QUser u, String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String lower = "%" + keyword.toLowerCase() + "%";
        return c.name.lower().like(lower)
                .or(c.description.lower().like(lower))
                .or(u.nickname.lower().like(lower));
    }

    private BooleanExpression cursorCondition(QCharacterCard c, CharacterSortType sort, UUID cursor) {
        if (cursor == null) return null;

        Long cursorId = queryFactory
                .select(c.id)
                .from(c)
                .where(c.publicId.eq(cursor))
                .fetchOne();
        if (cursorId == null) return null;

        if (sort == CharacterSortType.POPULAR) {
            Long cursorUseCnt = queryFactory.select(c.useCnt).from(c).where(c.id.eq(cursorId)).fetchOne();
            Instant cursorCreatedAt = queryFactory.select(c.createdAt).from(c).where(c.id.eq(cursorId)).fetchOne();
            return c.useCnt.lt(cursorUseCnt)
                    .or(c.useCnt.eq(cursorUseCnt).and(c.createdAt.lt(cursorCreatedAt)))
                    .or(c.useCnt.eq(cursorUseCnt).and(c.createdAt.eq(cursorCreatedAt)).and(c.id.lt(cursorId)));
        } else {
            Instant cursorCreatedAt = queryFactory.select(c.createdAt).from(c).where(c.id.eq(cursorId)).fetchOne();
            return c.createdAt.lt(cursorCreatedAt)
                    .or(c.createdAt.eq(cursorCreatedAt).and(c.id.lt(cursorId)));
        }
    }

    private OrderSpecifier<?>[] orderBy(QCharacterCard c, CharacterSortType sort) {
        if (sort == CharacterSortType.POPULAR) {
            return new OrderSpecifier[]{c.useCnt.desc(), c.createdAt.desc(), c.id.desc()};
        }
        return new OrderSpecifier[]{c.createdAt.desc(), c.id.desc()};
    }

    @Override
    public List<UserCharacterCardListItem> findByCreator(
            Long creatorId,
            boolean includePrivate,
            UUID cursor,
            int size
    ) {
        QCharacterCard c = QCharacterCard.characterCard;

        return queryFactory
                .select(Projections.constructor(UserCharacterCardListItem.class,
                        c.publicId,
                        c.name,
                        c.code,
                        c.description,
                        c.imageUrl,
                        c.useCnt,
                        c.createdAt,
                        c.updatedAt))
                .from(c)
                .where(
                        c.creator.userId.eq(creatorId),
                        c.isDeleted.isFalse(),
                        publicOnlyCondition(c, includePrivate),
                        cursorConditionLatest(c, cursor)
                )
                .orderBy(c.createdAt.desc(), c.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression publicOnlyCondition(QCharacterCard c, boolean includePrivate) {
        if (includePrivate) return null;
        return c.isPublic.isTrue();
    }

    private BooleanExpression cursorConditionLatest(QCharacterCard c, UUID cursor) {
        if (cursor == null) return null;

        Long cursorId = queryFactory
                .select(c.id)
                .from(c)
                .where(c.publicId.eq(cursor))
                .fetchOne();
        if (cursorId == null) return null;

        Instant cursorCreatedAt = queryFactory
                .select(c.createdAt)
                .from(c)
                .where(c.id.eq(cursorId))
                .fetchOne();

        return c.createdAt.lt(cursorCreatedAt)
                .or(c.createdAt.eq(cursorCreatedAt).and(c.id.lt(cursorId)));
    }
}
