package org.apereo.cas.webauthn.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.core.WebAuthnServer;
import com.yubico.data.AssertionRequestWrapper;
import com.yubico.data.RegistrationRequest;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link WebAuthnController}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RestController("webAuthnController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(WebAuthnController.BASE_ENDPOINT_WEBAUTHN)
public class WebAuthnController {
    /**
     * Base endpoint for all webauthn web resources.
     */
    public static final String BASE_ENDPOINT_WEBAUTHN = "/webauthn";

    /**
     * webauthn registration endpoint.
     */
    public static final String WEBAUTHN_ENDPOINT_REGISTER = "/register";

    /**
     * webauthn authentication endpoint.
     */
    public static final String WEBAUTHN_ENDPOINT_AUTHENTICATE = "/authenticate";

    private static final String WEBAUTHN_ENDPOINT_FINISH = "/finish";

    private static final ObjectMapper MAPPER = JacksonCodecs.json().findAndRegisterModules();

    private final WebAuthnServer server;

    /**
     * Start registration and provide response entity.
     *
     * @param username           the username
     * @param displayName        the display name
     * @param credentialNickname the credential nickname
     * @param requireResidentKey the require resident key
     * @param sessionTokenBase64 the session token base 64
     * @param request            the request
     * @param response           the response
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_REGISTER, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> startRegistration(
        @NonNull
        @RequestParam("username")
        final String username,
        @NonNull
        @RequestParam("displayName")
        final String displayName,
        @RequestParam(value = "credentialNickname", required = false, defaultValue = StringUtils.EMPTY)
        final String credentialNickname,
        @RequestParam(value = "requireResidentKey", required = false)
        final boolean requireResidentKey,
        @RequestParam(value = "sessionToken", required = false, defaultValue = StringUtils.EMPTY)
        final String sessionTokenBase64,
        final HttpServletRequest request,
        final HttpServletResponse response)
        throws Exception {

        val result = server.startRegistration(
            username,
            Optional.of(displayName),
            Optional.ofNullable(credentialNickname),
            requireResidentKey,
            Optional.ofNullable(sessionTokenBase64).map(Unchecked.function(ByteArray::fromBase64Url)));

        if (result.isRight()) {
            return startResponse(new StartRegistrationResponse(result.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().get());
    }

    /**
     * Finish registration and provide response entity.
     *
     * @param responseJson the response json
     * @return the response entity
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_REGISTER + WEBAUTHN_ENDPOINT_FINISH, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> finishRegistration(@RequestBody final String responseJson) {
        val result = server.finishRegistration(responseJson);
        return finishResponse(result, responseJson);
    }

    /**
     * Start authentication and provide response entity.
     *
     * @param username the username
     * @return the response entity
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_AUTHENTICATE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> startAuthentication(@RequestParam(value = "username", required = false) final String username) {
        val request = server.startAuthentication(Optional.ofNullable(username));
        if (request.isRight()) {
            return startResponse(new StartAuthenticationResponse(request.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), request.left().get());
    }

    /**
     * Finish authentication and create response entity.
     *
     * @param responseJson the response json
     * @return the response entity
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_AUTHENTICATE + WEBAUTHN_ENDPOINT_FINISH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> finishAuthentication(@RequestBody final String responseJson) {
        val result = server.finishAuthentication(responseJson);
        return finishResponse(result, responseJson);
    }

    private static ResponseEntity<Object> startResponse(final Object request) {
        LOGGER.trace("Response: [{}]", request);
        return ResponseEntity.ok(writeJson(request));
    }

    @SneakyThrows
    private static String writeJson(final Object o) {
        return MAPPER.writeValueAsString(o);
    }

    private static ResponseEntity<Object> finishResponse(final Either<List<String>, ?> result, final String responseJson) {
        if (result.isRight()) {
            LOGGER.trace("Response: [{}]", responseJson);
            return ResponseEntity.ok(writeJson(result.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().get());
    }

    private static ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final String message) {
        return messagesJson(response, List.of(message));
    }

    private static ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final List<String> messages) {
        return response.body(Map.of("messages", messages));
    }

    @RequiredArgsConstructor
    @Getter
    private static class StartAuthenticationResponse {
        private final boolean success = true;

        private final AssertionRequestWrapper request;

        private final StartAuthenticationActions actions = new StartAuthenticationActions();
    }

    @RequiredArgsConstructor
    @Getter
    private static class StartRegistrationResponse {
        private final boolean success = true;

        private final RegistrationRequest request;

        private final StartRegistrationActions actions = new StartRegistrationActions();
    }

    @Getter
    private static class StartRegistrationActions {
        private final String finish = BASE_ENDPOINT_WEBAUTHN.substring(1) + WEBAUTHN_ENDPOINT_REGISTER + WEBAUTHN_ENDPOINT_FINISH;
    }

    @Getter
    private static class StartAuthenticationActions {
        private final String finish = BASE_ENDPOINT_WEBAUTHN.substring(1) + WEBAUTHN_ENDPOINT_AUTHENTICATE + WEBAUTHN_ENDPOINT_FINISH;
    }
}
