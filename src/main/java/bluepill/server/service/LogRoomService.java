package bluepill.server.service;

import bluepill.server.client.AgentClient;
import bluepill.server.domain.CharacterCard;
import bluepill.server.domain.CharacterSnapshot;
import bluepill.server.domain.ExampleDialogue;
import bluepill.server.domain.LogPhoto;
import bluepill.server.domain.LogRoom;
import bluepill.server.domain.LogRoomMember;
import bluepill.server.domain.LogRoomRelationship;
import bluepill.server.domain.User;
import bluepill.server.dto.logroom.DayLogEntry;
import bluepill.server.dto.logroom.DayLogTimeSlot;
import bluepill.server.dto.logroom.LogCharacterCardResponse;
import bluepill.server.dto.logroom.LogPhotoUploadRequest;
import bluepill.server.dto.logroom.LogPhotoUploadResponse;
import bluepill.server.dto.logroom.LogRoomCreateRequest;
import bluepill.server.dto.logroom.LogRoomCreateResponse;
import bluepill.server.dto.logroom.LogRoomListItem;
import bluepill.server.dto.logroom.LogRoomListResponse;
import bluepill.server.dto.logroom.LogRoomParticipant;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.character.CharacterCardRepository;
import bluepill.server.repository.chat.ChatMessageRepository;
import bluepill.server.repository.logroom.CharacterPhotoRow;
import bluepill.server.repository.character.CharacterSnapshotRepository;
import bluepill.server.repository.logroom.DayLogRow;
import bluepill.server.repository.logroom.LogPhotoRepository;
import bluepill.server.repository.logroom.LogRoomMemberRepository;
import bluepill.server.repository.logroom.LogRoomPageRow;
import bluepill.server.repository.logroom.LogRoomRelationshipRepository;
import bluepill.server.repository.logroom.LogRoomRepository;
import bluepill.server.repository.logroom.MemberImageRow;
import bluepill.server.util.ImageUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
    private final ImageUrlBuilder imageUrlBuilder;
    private final ImageStorageService imageStorageService;
    private final ChatMessageRepository chatMessageRepository;
    private final AgentClient agentClient;
    private final PlatformTransactionManager transactionManager;

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

        // 방별 creatorUserId 매핑
        Map<Long, Long> creatorByRoom = page.stream()
                .collect(Collectors.toMap(LogRoomPageRow::roomId, LogRoomPageRow::creatorUserId));

        // 방별 그룹화: 멤버 수 + 참가자 배열
        Map<Long, List<LogRoomParticipant>> participantsByRoom = new LinkedHashMap<>();
        Map<Long, Long> countByRoom = new HashMap<>();
        for (MemberImageRow row : memberImages) {
            countByRoom.merge(row.roomId(), 1L, Long::sum);
            boolean isUser = row.memberUserId() != null;
            boolean isOwner = isUser && row.memberUserId().equals(creatorByRoom.get(row.roomId()));
            participantsByRoom.computeIfAbsent(row.roomId(), k -> new ArrayList<>())
                    .add(new LogRoomParticipant(row.memberPublicId(), row.memberName(), imageUrlBuilder.buildUrl(row.imageUrl()), isUser, isOwner));
        }

        // 쿼리3: 각 방의 캐릭터 사진 (postDate DESC, timeSlot DESC 정렬됨)
        List<CharacterPhotoRow> characterPhotos = logRoomRepository.findCharacterPhotosByRoomIds(roomIds);

        // 방별로 가장 최근 슬롯의 캐릭터 사진 후보 모으기
        Map<Long, List<String>> bgCandidatesByRoom = new LinkedHashMap<>();
        Map<Long, LocalDate> latestDateByRoom = new HashMap<>();
        Map<Long, Integer> latestSlotByRoom = new HashMap<>();
        for (CharacterPhotoRow row : characterPhotos) {
            Long roomId = row.roomId();
            LocalDate latestDate = latestDateByRoom.get(roomId);
            if (latestDate == null) {
                // 그 방의 첫 row = 가장 최근 슬롯
                latestDateByRoom.put(roomId, row.postDate());
                latestSlotByRoom.put(roomId, row.timeSlot());
                bgCandidatesByRoom.put(roomId, new ArrayList<>(List.of(row.imageUrl())));
            } else if (row.postDate().equals(latestDate)
                    && row.timeSlot().equals(latestSlotByRoom.get(roomId))) {
                // 같은 최근 슬롯
                bgCandidatesByRoom.get(roomId).add(row.imageUrl());
            }
            // 그 외(더 오래된 슬롯) : 스킵
        }

        // 후보 중 랜덤 1장 선택 : backgroundImageUrl
        Random random = new Random();
        Map<Long, String> bgByRoom = new HashMap<>();
        for (var entry : bgCandidatesByRoom.entrySet()) {
            List<String> candidates = entry.getValue();
            bgByRoom.put(entry.getKey(), candidates.get(random.nextInt(candidates.size())));
        }

        // LogRoomListItem 조립
        List<LogRoomListItem> content = page.stream()
                .map(r -> new LogRoomListItem(
                        r.publicId(),
                        r.name(),
                        r.isPublic(),
                        imageUrlBuilder.buildUrl(bgByRoom.get(r.roomId())),
                        countByRoom.getOrDefault(r.roomId(), 0L),
                        r.createdAt(),
                        r.creatorUserId().equals(viewerId),
                        r.creatorPublicId(),
                        r.creatorNickname(),
                        participantsByRoom.getOrDefault(r.roomId(), List.of())))
                .toList();

        UUID nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).getPublicId()
                : null;

        long total = logRoomMemberRepository.countByUser_UserId(viewerId);

        return new LogRoomListResponse(content, nextCursor, hasNext, total);
    }

    // 클래스가 @Transactional(readOnly=true)라, 이 메서드는 트랜잭션에서 빠져나온다.
    // LLM(chat_rule) 호출을 트랜잭션 밖에서 하여 커넥션 점유 시간을 줄이고,
    // DB 쓰기만 TransactionTemplate 으로 짧게 묶어 원자성을 유지한다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LogRoomCreateResponse createLogRoom(LogRoomCreateRequest request, Long creatorUserId) {
        // 1. 검증 (트랜잭션 밖)
        User creator = userService.findById(creatorUserId);

        UUID cardPublicId = request.getCharacterCardPublicIds().get(0);
        CharacterCard card = characterCardRepository.findByPublicIdAndIsDeletedFalse(cardPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

        // 비공개 카드인데 본인 게 아니면 403
        if (!card.getIsPublic() && !card.getCreator().getUserId().equals(creatorUserId)) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_PRIVATE);
        }

        // 2. chat_rule 생성 (트랜잭션 밖: LLM 대기 동안 커넥션 안 잡음)
        //     여기서 실패하면 예외로 종료 → 아래 트랜잭션 시작 안 함 → 방 안 생김(원자성 유지)
        String chatRule = agentClient.completeChatRule(
                new AgentClient.AgentChatRuleRequest(
                        String.valueOf(card.getId()),
                        request.getRelationship()
                )
        ).chatRule();

        // 3. DB 쓰기만 짧은 트랜잭션으로 일괄 처리
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            // 3-1. 재검증 (TOCTOU 방지) + 트랜잭션 내 관리 엔티티 확보(지연 로딩용)
            CharacterCard fresh = characterCardRepository.findByPublicIdAndIsDeletedFalse(cardPublicId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_CARD_NOT_FOUND));

            // 3-2. 스냅샷 find-or-create (현재 카드 버전 기준)
            CharacterSnapshot snapshot = characterSnapshotRepository
                    .findByCharacterIdAndVersion(fresh.getId(), fresh.getVersion())
                    .orElseGet(() -> characterSnapshotRepository.save(
                            CharacterSnapshot.builder()
                                    .characterId(fresh.getId())
                                    .version(fresh.getVersion())
                                    .name(fresh.getName())
                                    .description(fresh.getDescription())
                                    .prompt(fresh.getPrompt())
                                    .imageUrl(fresh.getImageUrl())
                                    .exampleDialogues(fresh.getExampleDialogues().stream()
                                            .map(ExampleDialogue::getContent)
                                            .toList())
                                    .build()
                    ));

            // 3-3. 로그방 생성
            LogRoom room = logRoomRepository.save(LogRoom.builder()
                    .publicId(UUID.randomUUID())
                    .name(request.getName())
                    .isPublic(request.getIsPublic())
                    .createdBy(creator)
                    .build());

            // 3-4. 멤버 생성
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

            // 3-5. 관계 + chat_rule 저장
            logRoomRelationshipRepository.save(LogRoomRelationship.builder()
                    .logRoom(room)
                    .memberA(humanMember)
                    .memberB(characterMember)
                    .label(request.getRelationship())
                    .chatRule(chatRule)
                    .build());

            return LogRoomCreateResponse.from(room);
        });
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
                            imageUrlBuilder.buildUrl(row.imageUrl()),
                            row.authorType(),
                            row.authorName(),
                            imageUrlBuilder.buildUrl(row.authorImageUrl()),
                            row.createdAt()
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
                imageUrlBuilder.buildUrl(snapshot.getImageUrl()),
                card.getUseCnt(),
                card.getIsDeleted(),
                card.getIsPublic(),
                isLatest,
                isOwner,
                canUpdate
        );
    }

    @Transactional
    public void updateLogCharacterCard(UUID roomPublicId, UUID memberPublicId, Long viewerId) {
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

        CharacterSnapshot currentSnapshot = member.getSnapshot();

        // live 카드 조회 (삭제 카드 포함)
        CharacterCard card = characterCardRepository.findById(currentSnapshot.getCharacterId())
                .orElseThrow();   // 데이터 불일치 시 500

        // 상태 검증: 삭제 → 이미 최신 → 권한
        if (card.getIsDeleted()) {
            throw new BusinessException(ErrorCode.CHARACTER_CARD_DELETED);
        }
        if (currentSnapshot.getVersion().equals(card.getVersion())) {
            throw new BusinessException(ErrorCode.ALREADY_LATEST_VERSION);
        }
        boolean isOwner = card.getCreator().getUserId().equals(viewerId);
        if (!card.getIsPublic() && !isOwner) {
            throw new BusinessException(ErrorCode.LOG_CHARACTER_UPDATE_FORBIDDEN);
        }

        // 최신 버전 스냅샷 find-or-create
        CharacterSnapshot latestSnapshot = characterSnapshotRepository
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

        // 멤버의 스냅샷 핀 교체
        member.updateSnapshot(latestSnapshot);
    }

    @Transactional
    public LogPhotoUploadResponse uploadPhoto(UUID roomPublicId, String timezoneHeader,
                                              LogPhotoUploadRequest request, Long viewerId) {
        // timezone 파싱
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezoneHeader);
        } catch (DateTimeException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_TIMEZONE);
        }

        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 사용자 멤버 조회 (멤버십 검증 겸용)
        LogRoomMember member = logRoomMemberRepository
                .findByLogRoom_IdAndUser_UserId(room.getId(), viewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN));

        // 현재 시각 기준 postDate, timeSlot 계산 (3시간 단위)
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate postDate = now.toLocalDate();
        Integer timeSlot = (now.getHour() / 3) * 3;

        // LogPhoto INSERT (unique 위반 시 409)
        LogPhoto photo;
        try {
            photo = logPhotoRepository.save(LogPhoto.builder()
                    .publicId(UUID.randomUUID())
                    .member(member)
                    .postDate(postDate)
                    .timeSlot(timeSlot)
                    .imageUrl(request.getImageUrl())
                    .caption(request.getCaption())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.PHOTO_ALREADY_UPLOADED);
        }

        return LogPhotoUploadResponse.from(photo, imageUrlBuilder.buildUrl(photo.getImageUrl()));
    }

    @Transactional
    public void deletePhoto(UUID roomPublicId, UUID photoPublicId, Long viewerId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 멤버십 체크
        boolean isMember = logRoomMemberRepository.existsByLogRoom_IdAndUser_UserId(room.getId(), viewerId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }

        // 사진 조회
        LogPhoto photo = logPhotoRepository.findByPublicId(photoPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_PHOTO_NOT_FOUND));

        // 방 소속 검증 (다른 방 사진 ID로 시도하는 경우 차단)
        if (!photo.getMember().getLogRoom().getId().equals(room.getId())) {
            throw new BusinessException(ErrorCode.LOG_PHOTO_NOT_FOUND);
        }

        // 작성자 본인 검증 (캐릭터 사진이거나 다른 사용자 사진이면 403)
        User author = photo.getMember().getUser();
        if (author == null || !author.getUserId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.LOG_PHOTO_FORBIDDEN);
        }

        // DB 삭제
        String imageKey = photo.getImageUrl();
        logPhotoRepository.delete(photo);

        // 트랜잭션 커밋 후 R2 객체 삭제 (커밋 실패 시 객체는 보존)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            imageStorageService.deleteImage(imageKey);
                        } catch (Exception e) {
                            log.warn("R2 객체 삭제 실패(고아 객체 남음): key={}", imageKey, e);
                        }
            }
        });
    }

    @Transactional
    public void deleteLogRoom(UUID roomPublicId, Long userId) {
        // 방 조회
        LogRoom room = logRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_ROOM_NOT_FOUND));

        // 방장(생성자)만 삭제 가능
        if (!room.getCreatedBy().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LOG_ROOM_FORBIDDEN);
        }
        Long roomId = room.getId();

        // R2 key 먼저 수집 (DB 삭제 전)
        List<String> imageKeys = logPhotoRepository.findImageUrlsByRoomId(roomId);

        // FK 안전 순서로 DB 삭제 (posts 는 log_rooms ON DELETE CASCADE 로 자동 삭제)
        logRoomRelationshipRepository.deleteByRoomId(roomId);
        chatMessageRepository.deleteByRoomId(roomId);
        logPhotoRepository.deleteByRoomId(roomId);
        logRoomMemberRepository.deleteByRoomId(roomId);
        logRoomRepository.delete(room);

        // 커밋 후 R2 객체 정리 (커밋 실패 시 객체 보존)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String key : imageKeys) {
                    try {
                        imageStorageService.deleteImage(key);
                    } catch (Exception e) {
                        log.warn("R2 객체 삭제 실패(고아 객체 남음): key={}", key, e);
                    }
                }
                // agent 데이터(daily_plans + LangGraph 기억) 정리 (실패해도 방 삭제는 유지)
                try {
                    agentClient.deleteLogRoomData(roomId);
                } catch (Exception e) {
                    log.warn("agent 데이터 삭제 실패(고아 남음): roomId={}", roomId, e);
                }
            }
        });
    }
}
