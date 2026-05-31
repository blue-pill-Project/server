package bluepill.server.service;

import bluepill.server.dto.logroom.LogRoomListItem;
import bluepill.server.dto.logroom.LogRoomListResponse;
import bluepill.server.repository.LogRoomPageRow;
import bluepill.server.repository.LogRoomRepository;
import bluepill.server.repository.MemberImageRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
