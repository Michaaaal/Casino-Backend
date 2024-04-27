package michal.malek.auth.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    private final ClientRegistration googleClientRegistration;

    public OAuth2AccessToken codeToAccessToken(String code) throws RuntimeException{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(code);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        code = rootNode.path("code").asText();

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    googleClientRegistration.getClientId(),
                    googleClientRegistration.getClientSecret(),
                    code,
                    googleClientRegistration.getRedirectUri())
                    .execute();

            String accessTokenValue = tokenResponse.getAccessToken();
            return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessTokenValue, Instant.now(), Instant.now().plusSeconds(3600), Collections.emptySet());

        }catch (Exception e){
            throw new RuntimeException("something went wrong");
        }
    }

    public OAuth2AuthenticationToken accessTokenToAuthToken(OAuth2AccessToken accessToken){
        OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = new DefaultOAuth2UserService();
        OAuth2UserRequest userRequest = new OAuth2UserRequest(googleClientRegistration, accessToken);
        OAuth2User user = userService.loadUser(userRequest);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new OAuth2AuthenticationToken(user, authorities, googleClientRegistration.getRegistrationId());
    }

}
