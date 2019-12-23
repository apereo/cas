package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.webauthn.registration.WebAuthnRegistrationRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.internal.util.WebAuthnCodecs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Optional;

/**
 * This is {@link WebAuthnRestController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("webAuthnRestController")
@Slf4j
@RequestMapping(value = "/webauthn", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WebAuthnRestController {
    private static final String WEBAUTHN_CONTEXT_PATH = "/webauthn";

    private final WebAuthnOperations server;

    private final CasConfigurationProperties casProperties;

    private final ObjectMapper jsonMapper = WebAuthnCodecs.json().findAndRegisterModules();

    @GetMapping
    public ResponseEntity index() throws Exception {
        val prefix = casProperties.getServer().getPrefix();
        val addCredential = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/addCredential"));
        val authenticate = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/authenticate"));
        val deleteAccount = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/deleteAccount"));
        val deregister = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/deregister"));
        val register = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/register"));

        val actions = CollectionUtils.wrap("addCredential", addCredential,
            "authenticate", authenticate, "deleteAccount", deleteAccount,
            "deregister", deregister, "register", register);
        return new ResponseEntity<>(CollectionUtils.wrap("actions", actions), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity startRegistration(
        @RequestParam("username") final String username,
        @RequestParam("displayName") final String displayName,
        @RequestParam("credentialNickname") final String credentialNickname,
        @RequestParam(value = "requireResidentKey", defaultValue = "false") @DefaultValue("false") final boolean requireResidentKey) throws Exception {
        val result = server.startRegistration(
            username,
            displayName,
            Optional.ofNullable(credentialNickname),
            requireResidentKey
        );

        if (result.isRight()) {
            return new ResponseEntity<>(jsonMapper.writeValueAsString(new StartRegistrationResponse(result.right().get())), HttpStatus.OK);
        }
        return new ResponseEntity<>(jsonMapper.writeValueAsString(result.left().get()), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/finish")
    public ResponseEntity finishRegistration(final String responseJson) throws Exception {
        val result = server.finishRegistration(responseJson);

        if (result.isRight()) {
            try {
                return new ResponseEntity<>(jsonMapper.writeValueAsString(result.right().get()), HttpStatus.OK);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                return new ResponseEntity<>("Attestation verification failed", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(jsonMapper.writeValueAsString(result.left().get()), HttpStatus.BAD_REQUEST);
    }

    @RequiredArgsConstructor
    @Getter
    private class StartRegistrationResponse {
        private final boolean success = true;

        private final WebAuthnRegistrationRequest request;

        private final StartRegistrationActions actions = new StartRegistrationActions();
    }

    @Getter
    private class StartRegistrationActions {
        private final URL finish;

        @SneakyThrows
        StartRegistrationActions() {
            val prefix = casProperties.getServer().getPrefix();
            finish = new URL(prefix.concat(WEBAUTHN_CONTEXT_PATH + "/finish"));
        }
    }
}
