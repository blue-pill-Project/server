package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.dto.user.UserProfileResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByPublicId(UUID publicId) {
        return userRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public UserProfileResponse getProfile(UUID publicId, Long loginUserId) {
        User user = userRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isOwner = loginUserId != null && loginUserId.equals(user.getUserId());

        //todo: 캐릭터, 게시물 개수
        long characterCnt = 0L;
        long postCnt = 0L;

        return new UserProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                user.getImageUrl(),
                user.getEmail(),
                user.getPlan() != null ? user.getPlan().getId() : null,
                user.getIsPublic(),
                characterCnt,
                postCnt,
                isOwner
        );
    };
}
