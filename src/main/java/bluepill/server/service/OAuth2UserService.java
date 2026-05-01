package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        //구글에서 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());

        String providerId = oAuth2User.getAttribute("sub");  // 구글 고유 ID
        String email = oAuth2User.getAttribute("email");
        String imageUrl = oAuth2User.getAttribute("picture");

        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.save(User.createNewUser(providerId, provider, email, imageUrl)));

        return oAuth2User;
    }

}
