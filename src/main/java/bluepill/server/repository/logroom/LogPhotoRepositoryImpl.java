package bluepill.server.repository.logroom;

import bluepill.server.domain.QCharacterSnapshot;
import bluepill.server.domain.QLogPhoto;
import bluepill.server.domain.QLogRoomMember;
import bluepill.server.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class LogPhotoRepositoryImpl implements LogPhotoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DayLogRow> findDayLog(Long roomId, LocalDate date) {
        QLogPhoto p = QLogPhoto.logPhoto;
        QLogRoomMember m = QLogRoomMember.logRoomMember;
        QUser u = QUser.user;
        QCharacterSnapshot s = QCharacterSnapshot.characterSnapshot;

        return queryFactory
                .select(Projections.constructor(DayLogRow.class,
                        p.timeSlot,
                        m.publicId,
                        p.publicId,
                        p.caption,
                        p.imageUrl,
                        new CaseBuilder()
                                .when(m.user.isNotNull()).then("USER")
                                .otherwise("CHARACTER"),
                        s.name.coalesce(u.nickname),       // 캐릭터면 스냅샷 이름, 사람이면 닉네임
                        s.imageUrl.coalesce(u.imageUrl)))  // 캐릭터면 스냅샷 이미지, 사람이면 프로필
                .from(p)
                .join(p.member, m)
                .leftJoin(m.user, u)
                .leftJoin(m.snapshot, s)
                .where(m.logRoom.id.eq(roomId)
                        .and(p.postDate.eq(date)))
                .orderBy(p.timeSlot.asc(), m.createdAt.asc())
                .fetch();
    }
}
