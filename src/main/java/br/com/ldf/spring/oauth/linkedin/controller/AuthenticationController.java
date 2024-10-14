package br.com.ldf.spring.oauth.linkedin.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping(AuthenticationController.AUTHENTICATION_ENDPOINT_PATH)
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    public static final String AUTHENTICATION_ENDPOINT_PATH = "/api/authentication";

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcUser> authenticate(@AuthenticationPrincipal OidcUser oidcUser) {
        return ResponseEntity.ok(oidcUser);
    }
}
