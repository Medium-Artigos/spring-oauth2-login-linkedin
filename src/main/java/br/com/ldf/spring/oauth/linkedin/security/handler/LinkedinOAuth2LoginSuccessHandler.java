package br.com.ldf.spring.oauth.linkedin.security.handler;

import br.com.ldf.spring.oauth.linkedin.persistence.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

import static br.com.ldf.spring.oauth.linkedin.controller.AuthenticationController.AUTHENTICATION_ENDPOINT_PATH;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LinkedinOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttribute("email");
        Objects.requireNonNull(email);

        userRepository.findByEmail(email).ifPresent(
            user -> {
                log.info("stage=on-authentication-success, message=person-found, person={}", user);
                request.getSession().setAttribute("person", user);
            });

        response.sendRedirect(AUTHENTICATION_ENDPOINT_PATH);
    }

}
