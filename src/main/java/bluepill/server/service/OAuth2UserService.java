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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());

        String providerId = getProviderId(provider, oAuth2User);
        String email = oAuth2User.getAttribute("email");

        userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.save(User.createNewUser(providerId, provider, email)));

        return oAuth2User;
    }

    private String getProviderId(User.Provider provider, OAuth2User oAuth2User) {
        return switch(provider){
            case GOOGLE -> oAuth2User.getAttribute("sub");
            case DISCORD -> oAuth2User.getAttribute("id");
        };
    }
}
