package bluepill.server.service;

import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.CharacterSnapshot;
import bluepill.server.domain.ExampleDialogue;
import bluepill.server.domain.LogRoom;
import bluepill.server.domain.LogRoomMember;
import bluepill.server.domain.LogRoomRelationship;
import bluepill.server.domain.User;
import bluepill.server.dto.logroom.DayLogEntry;
import bluepill.server.dto.logroom.DayLogTimeSlot;
import bluepill.server.dto.logroom.LogCharacterCardResponse;
import bluepill.server.dto.logroom.LogRoomCreateRequest;
import bluepill.server.dto.logroom.LogRoomCreateResponse;
import bluepill.server.dto.logroom.LogRoomListItem;
import bluepill.server.dto.logroom.LogRoomListResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.CharacterCardRepository;
import bluepill.server.repository.CharacterSnapshotRepository;
import bluepill.server.repository.DayLogRow;
import bluepill.server.repository.LogPhotoRepository;
import bluepill.server.repository.LogRoomMemberRepository;
import bluepill.server.repository.LogRoomPageRow;
import bluepill.server.repository.LogRoomRelationshipRepository;
import bluepill.server.repository.LogRoomRepository;
import bluepill.server.repository.MemberImageRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogRoomService {

    private final LogRoomRepository logRoomRepository;
    private final LogRoomMemberRepository logRoomMemberRepository;
    private final LogRoomRelationshipRepository logRoomRelationshipRepository;
    private final LogPhotoRepository logPhotoRepository;
    private final CharacterCardRepository characterCardRepository;
    private final CharacterSnapshotRepository characterSnapshotRepository;
    private final UserService userService;

    public LogRoomListResponse getMyLogRooms(Long viewerId, UUID cursor, int size) {
        // 쿼리1: 방 페이지(+방장)
        List<LogRoomPageRow> page = logRoomRepository.findMyLogRoomsPage(viewerId, cursor, size);

        boolean hasNext = page.size() > size;
        if (hasNext) {
            page = page.subList(0, size);
        }

        // 쿼리 2: 그 방들의 멤버 이미지
        List<Long> roomIds = page.stream().map(LogRoomPageRow::roomId).toList();
        List<MemberImageRow> memberImages = logRoomRepository.findMemberImagesByRoomIds(roomIds);

        // 방별 그룹화: 멤버 수 + 이미지 배열 (이미지 null은 배열에서 제외, 카운트는 전체 멤버)
        Map<Long, List<String>> imagesByRoom = new LinkedHashMap<>();
        Map<Long, Long> countByRoom = new HashMap<>();
        for (MemberImageRow row : memberImages) {
            countByRoom.merge(row.roomId(), 1L, Long::sum);
            if (row.imageUrl() != null) {
                imagesByRoom.computeIfAbsent(row.roomId(), k -> new ArrayList<>()).add(row.imageUrl());
            }
        }

        // LogRoomListItem 조립
        List<LogRoomListItem> content = page.stream()
                .map(r -> new LogRoomListItem(
                        r.publicId(),
                        r.name(),
                        countByRoom.getOrDefault(r.roomId(), 0L),
                        r.createdAt(),
                        r.creatorUserId().equals(viewerId),
                        r.creatorPublicId(),
                        r.creatorNickname(),
                        imagesByRoom.getOrDefault(r.roomId(), List.of())))
                .toList();

        UUID nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).getPublicId()
                : null;

        return new LogRoomListResponse(content, nextCursor, hasNext);
    }

    @Transactional
    public LogRoomCreateResponse createLogRoom(LogRoomCreateRequest request, Long creatorUserId) {
        // 생성자(방장) 조회
        User creator = userService.findById(creatorUserId);

        // 캐릭터 카드 조회 + 검증 (1명만 허용은 DTO에서 검증)
        UUID cardPublicId = request.getCharacterCardPublicIds().get(0);
        CharacterCard card = characterCardRepository.findByPublicIdAndIsDeletedFalse(cardPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        // 비공개 카드인데 본인 게 아니면 403
        if (!card.getIsPublic() && !card.getCreator().getUserId().equals(creatorUserId)) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_PRIVATE);
        }

        // 스냅샷 find-or-create (현재 카드 버전 기준)
        CharacterSnapshot snapshot = characterSnapshotRepository
                .findByCharacterIdAndVersion(card.getId(), card.getVersion())
                .orElseGet(() -> characterSnapshotRepository.save(
                        CharacterSnapshot.builder()
                                .characterId(card.getId())
                                .version(card.getVersion())
                                .name(card.getName())
                                .description(card.getDescription())
                                .prompt(card.getPrompt())
                                .imageUrl(card.getImageUrl())
                                .exampleDialogues(card.getExampleDialogues().stream()
                                        .map(ExampleDialogue::getContent)
                                        .toList())
                                .build()
                ));

        // 로그방 생성
        LogRoom room = logRoomRepository.save(LogRoom.builder()
                .publicId(UUID.randomUUID())
                .name(request.getName())
                .isPublic(request.getIsPublic())
                .createdBy(creator)
                .build());

        // 멤버 생성
        LogRoomMember humanMember = logRoomMemberRepository.save(LogRoomMember.builder()
                .publicId(UUID.randomUUID())
                .logRoom(room)
                .user(creator)
                .snapshot(null)
                .build());

        LogRoomMember characterMember = logRoomMemberRepository.save(LogRoomMember.builder()
                .publicId(UUID.randomUUID())
                .logRoom(room)
                .user(null)
                .snapshot(snapshot)
                .build());

        // 관계 저장
        logRoomRelationshipRepository.save(LogRoomRelationship.builder()
                .logRoom(room)
                .memberA(humanMember)
                .memberB(characterMember)
                .label(request.getRelationship())
                .build());

        return LogRoomCreateResponse.from(room);
    }

    public List<DayLogTimeSlot> getDayLog(UUID roomPublicId, LocalDate date, Long viewerId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크 (사람 멤버 기준)
        boolean isMember = logRoomMemberRepository.existsByLogRoom_IdAndUser_UserId(room.getId(), viewerId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }

        // 사진 조회 (timeSlot ASC, createdAt ASC 정렬됨)
        List<DayLogRow> rows = logPhotoRepository.findDayLog(room.getId(), date);

        // timeSlot 기준 그룹화 (LinkedHashMap으로 정렬 순서 유지)
        Map<Integer, List<DayLogEntry>> grouped = new LinkedHashMap<>();
        for (DayLogRow row : rows) {
            grouped.computeIfAbsent(row.timeSlot(), k -> new ArrayList<>())
                    .add(new DayLogEntry(
                            row.memberPublicId(),
                            row.photoPublicId(),
                            row.caption(),
                            row.imageUrl(),
                            row.authorType(),
                            row.authorName(),
                            row.authorImageUrl()
                    ));
        }

        // DayLogTimeSlot 리스트로 변환
        return grouped.entrySet().stream()
                .map(e -> new DayLogTimeSlot(e.getKey(), e.getValue()))
                .toList();
    }

    public LogCharacterCardResponse getLogCharacterCard(UUID roomPublicId, UUID memberPublicId, Long viewerId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크
        boolean isMember = logRoomMemberRepository.existsByLogRoom_IdAndUser_UserId(room.getId(), viewerId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }

        // 멤버 조회 + 방 소속 + 캐릭터 멤버 여부 검증
        LogRoomMember member = logRoomMemberRepository.findByPublicId(memberPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_MEMBER_NOT_FOUND));

        if (!member.getLogRoom().getId().equals(room.getId()) || member.getSnapshot() == null) {
            throw new BusinessException(ErrorCode.LOG_ROOM_MEMBER_NOT_FOUND);
        }

        CharacterSnapshot snapshot = member.getSnapshot();

        // live 카드 조회 (삭제 카드 포함 — isDeleted 플래그 응답에 필요)
        CharacterCard card = characterCardRepository.findById(snapshot.getCharacterId())
                .orElseThrow();   // 데이터 불일치 시 500

        boolean isLatest = snapshot.getVersion().equals(card.getVersion());
        boolean isOwner = card.getCreator().getUserId().equals(viewerId);
        boolean canUpdate = !card.getIsDeleted() && !isLatest && (isOwner || card.getIsPublic());

        return new LogCharacterCardResponse(
                member.getPublicId(),
                card.getPublicId(),
                snapshot.getName(),
                snapshot.getDescription(),
                snapshot.getImageUrl(),
                card.getUseCnt(),
                card.getIsDeleted(),
                card.getIsPublic(),
                isLatest,
                isOwner,
                canUpdate
        );
    }
}
