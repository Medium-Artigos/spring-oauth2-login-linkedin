package br.com.ldf.spring.oauth.linkedin.security.service;

import br.com.ldf.spring.oauth.linkedin.persistence.entity.Role;
import br.com.ldf.spring.oauth.linkedin.persistence.entity.User;
import br.com.ldf.spring.oauth.linkedin.persistence.entity.UserRole;
import br.com.ldf.spring.oauth.linkedin.persistence.repository.RoleRepository;
import br.com.ldf.spring.oauth.linkedin.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class LinkedinOidUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    UserRepository userRepository;
    RoleRepository roleRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        final OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);
        List<GrantedAuthority> mappedAuthorities = new ArrayList<>(oidcUser.getAuthorities());

        String email = Objects.requireNonNull(oidcUser.getAttribute("email"));

        var user = userRepository.findByEmail(email)
            .orElseGet(() -> createNewUser(oidcUser));

        // Adicionando suas próprias authorities
        user.getRoles().forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority(role.getRole().getName())));

        // Retornando um novo OAuth2User com as autoridades modificadas
        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private User createNewUser(OAuth2User oAuth2User) {
        String name = Objects.requireNonNull(oAuth2User.getAttribute("name"));
        String email = Objects.requireNonNull(oAuth2User.getAttribute("email"));
        var role = roleRepository.findByName(Role.ROLE_USER).orElseThrow(() -> new RuntimeException("Role not found"));

        var user = User.builder()
            .name(name)
            .email(email)
            .build();

        user.setRoles(List.of(UserRole.builder().user(user).role(role).build()));

        return userRepository.save(user);
    }
}
