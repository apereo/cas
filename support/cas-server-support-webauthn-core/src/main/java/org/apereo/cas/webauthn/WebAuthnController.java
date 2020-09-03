package org.apereo.cas.webauthn;

import org.apereo.cas.util.LoggingUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.core.WebAuthnServer;
import com.yubico.webauthn.data.AssertionRequestWrapper;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RegistrationRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    @PostMapping(WEBAUTHN_ENDPOINT_REGISTER)
    public ResponseEntity<Object> startRegistration(
        @NonNull @RequestParam("username") final String username,
        @NonNull @RequestParam("displayName") final String displayName,
        @RequestParam(value = "credentialNickname", required = false, defaultValue = StringUtils.EMPTY) final String credentialNickname,
        @RequestParam(value = "requireResidentKey", required = false) final boolean requireResidentKey,
        @RequestParam(value = "sessionToken", required = false, defaultValue = StringUtils.EMPTY) final String sessionTokenBase64)
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

    @PostMapping(WEBAUTHN_ENDPOINT_REGISTER + WEBAUTHN_ENDPOINT_FINISH)
    public ResponseEntity finishRegistration(@RequestBody final String responseJson) throws Exception {
        val result = server.finishRegistration(responseJson);
        return finishResponse(result, "Attestation verification failed", responseJson);
    }

    @PostMapping(WEBAUTHN_ENDPOINT_AUTHENTICATE)
    public ResponseEntity<Object> startAuthentication(@RequestParam("username") final String username) {
        val request = server.startAuthentication(Optional.ofNullable(username));
        if (request.isRight()) {
            return startResponse(new StartAuthenticationResponse(request.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), request.left().get());
    }

    @PostMapping(WEBAUTHN_ENDPOINT_AUTHENTICATE + WEBAUTHN_ENDPOINT_FINISH)
    public ResponseEntity finishAuthentication(@RequestBody final String responseJson) {
        val result = server.finishAuthentication(responseJson);
        return finishResponse(result, "Authentication verification failed", responseJson);
    }

    @PostMapping("/deregister")
    public ResponseEntity deregisterCredential(
        @NonNull @RequestParam("sessionToken") final String sessionTokenBase64,
        @NonNull @RequestParam("credentialId") final String credentialIdBase64) {
        try {
            val credentialId = ByteArray.fromBase64Url(credentialIdBase64);
            val result = server.deregisterCredential(ByteArray.fromBase64Url(sessionTokenBase64), credentialId);

            if (result.isRight()) {
                return finishResponse(result, "Failed to deregister credential", StringUtils.EMPTY);
            }
            return messagesJson(ResponseEntity.badRequest(), result.left().get());

        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return messagesJson(ResponseEntity.badRequest(), "Invalid credential id" + credentialIdBase64);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity deleteAccount(@NonNull @RequestParam("username") final String username) {
        val result = server.deleteAccount(username, () ->
            ((ObjectNode) jsonFactory.objectNode()
                .set("success", jsonFactory.booleanNode(true)))
                .set("deletedAccount", jsonFactory.textNode(username))
        );

        if (result.isRight()) {
            return ResponseEntity.ok(writeJson(result.right().get().toString()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().get());
    }

    private static ResponseEntity<Object> jsonFail() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("{\"messages\":[\"Failed to encode response as JSON\"]}");
    }

    private static ResponseEntity<Object> startResponse(final Object request) {
        try {
            return ResponseEntity.ok(writeJson(request));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return jsonFail();
        }
    }

    @SneakyThrows
    private static String writeJson(final Object o) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    private ResponseEntity<Object> finishResponse(final Either<List<String>, ?> result, final String jsonFailMessage,
                                                  final String responseJson) {
        if (result.isRight()) {
            LOGGER.trace("Response: [{}]", responseJson);
            return ResponseEntity.ok(writeJson(result.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().get());
    }

    private ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final String message) {
        return messagesJson(response, List.of(message));
    }

    private ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final List<String> messages) {
        try {
            return response.body(
                writeJson(jsonFactory.objectNode()
                    .set("messages", jsonFactory.arrayNode()
                        .addAll(messages.stream().map(jsonFactory::textNode).collect(Collectors.toList()))
                    )
                ));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return jsonFail();
        }
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
