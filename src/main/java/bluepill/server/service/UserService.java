package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.dto.user.UserProfileResponse;
import bluepill.server.dto.user.UserProfileUpdateRequest;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.repository.CharacterCardRepository;
import bluepill.server.repository.UserRepository;
import bluepill.server.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CharacterCardRepository characterCardRepository;
    private final NicknameGenerator nicknameGenerator;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByPublicId(UUID publicId) {
        return userRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public UserProfileResponse getProfile(UUID publicId, Long userId) {
        User user = userRepository.findByPublicIdAndIsDeletedFalse(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isOwner = userId != null && userId.equals(user.getUserId());

        long characterCnt = characterCardRepository.countByCreatorAndIsDeletedFalse(user);

        return new UserProfileResponse(
                user.getPublicId(),
                user.getNickname(),
                user.getImageUrl(),
                user.getEmail(),
                user.getPlan() != null ? user.getPlan().getId() : null,
                user.getIsPublic(),
                characterCnt,
                isOwner
        );
    };

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String nickname;

        //최초 로그인
        if (request.nickname() == null | request.nickname().isBlank()) {
            if(user.getNickname() == null|| user.getNickname().isBlank()) {
                //랜덤 닉네임 부여
                nickname = nicknameGenerator.generate();
            }else {
                //기존 닉네임 있지만 빈칸 -> 거부
                throw new BusinessException(ErrorCode.NICKNAME_MISSING);
            }
        } else {
            nickname = request.nickname();
        }
        user.updateProfile(nickname, request.imageUrl());
        return UserProfileResponse.from(user,true);
    }

    @Transactional
    public boolean toggleVisibility(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.togglePublic();
        //캐릭터 카드 일괄 업데이트
        characterCardRepository.updateIsPublicByCreator(userId, user.getIsPublic());

        return user.getIsPublic();
    }
}
