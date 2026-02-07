package com.dev.XRail.security.oauth;

import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.SocialProvider;
import com.dev.XRail.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, attributes);

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        Member member = saveOrUpdate(oAuth2UserInfo);

        return new CustomOAuth2User(member, attributes);
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equalsIgnoreCase(registrationId)) {
            return new KakaoUserInfo(attributes);
        } else if ("naver".equalsIgnoreCase(registrationId)) {
            return new NaverUserInfo(attributes);
        }
        return null;
    }

    private Member saveOrUpdate(OAuth2UserInfo userInfo) {
        SocialProvider provider = SocialProvider.valueOf(userInfo.getProvider().toUpperCase());
        String socialId = userInfo.getProviderId();

        Member member = memberRepository.findBySocialIdAndSocialProvider(socialId, provider)
                .orElse(null);

        if (member == null) {
            member = createMember(userInfo, provider);
        }

        return member;
    }

    private Member createMember(OAuth2UserInfo userInfo, SocialProvider provider) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String loginId = provider.name().toLowerCase() + "_" + userInfo.getProviderId();
        // If email is null (e.g. user didn't agree), use a dummy email to satisfy DB constraint
        String email = userInfo.getEmail() != null ? userInfo.getEmail() : loginId + "@social.dummy"; 
        
        return memberRepository.save(Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Dummy password
                .name(userInfo.getName() != null ? userInfo.getName() : "User_" + uuid)
                .email(email)
                .socialProvider(provider)
                .socialId(userInfo.getProviderId())
                .phone("") // Optional
                .birthDate("") // Optional
                .build());
    }
}
