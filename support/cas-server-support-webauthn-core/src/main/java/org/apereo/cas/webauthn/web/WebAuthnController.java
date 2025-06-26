package org.apereo.cas.webauthn.web;

import com.yubico.core.WebAuthnServer;
import com.yubico.data.AssertionRequestWrapper;
import com.yubico.data.RegistrationRequest;
import com.yubico.util.Either;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link WebAuthnController}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(BaseWebAuthnController.BASE_ENDPOINT_WEBAUTHN)
@ResponseBody
@Tag(name = "WebAuthN")
public class WebAuthnController extends BaseWebAuthnController {
    /**
     * webauthn registration endpoint.
     */
    public static final String WEBAUTHN_ENDPOINT_REGISTER = "/register";

    /**
     * webauthn authentication endpoint.
     */
    public static final String WEBAUTHN_ENDPOINT_AUTHENTICATE = "/authenticate";

    private static final String WEBAUTHN_ENDPOINT_FINISH = "/finish";


    private final WebAuthnServer server;

    private static ResponseEntity<Object> startResponse(final Object request) throws Exception {
        val json = writeJson(request);
        LOGGER.trace("Start: [{}]", json);
        return ResponseEntity.ok(json);
    }

    private static ResponseEntity<Object> finishResponse(final Either<List<String>, ?> result,
                                                         final String responseJson) throws Exception {
        if (result.isRight()) {
            LOGGER.trace("Received: [{}]", responseJson);
            val json = writeJson(result.right().orElseThrow());
            LOGGER.trace("Returned: [{}]", json);
            return ResponseEntity.ok(json);
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().orElseThrow());
    }

    private static ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final String message) {
        return messagesJson(response, List.of(message));
    }

    private static ResponseEntity<Object> messagesJson(final ResponseEntity.BodyBuilder response, final List<String> messages) {
        return response.body(Map.of("messages", messages));
    }

    /**
     * Start registration and provide response entity.
     *
     * @param displayName            the display name
     * @param credentialNickname     the credential nickname
     * @param requireResidentKey     the require resident key
     * @param sessionTokenBase64     the session token base 64
     * @param authenticatedPrincipal the authenticated principal
     * @param request                the request
     * @param response               the response
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_REGISTER, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start registration",
        parameters = {
            @Parameter(name = "displayName", in = ParameterIn.QUERY, required = true, description = "Display name"),
            @Parameter(name = "credentialNickname", in = ParameterIn.QUERY, required = false, description = "Credential nickname"),
            @Parameter(name = "requireResidentKey", in = ParameterIn.QUERY, required = false, description = "Require resident key"),
            @Parameter(name = "sessionToken", in = ParameterIn.QUERY, required = false, description = "Session token")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "RegistrationRequest JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegistrationRequest.class)
            )
        ))
    public ResponseEntity<Object> startRegistration(
        @NonNull
        @RequestParam("displayName")
        final String displayName,
        @RequestParam(value = "credentialNickname", required = false, defaultValue = StringUtils.EMPTY)
        final String credentialNickname,
        @RequestParam(value = "requireResidentKey", required = false)
        final boolean requireResidentKey,
        @RequestParam(value = "sessionToken", required = false, defaultValue = StringUtils.EMPTY)
        final String sessionTokenBase64,
        final Principal authenticatedPrincipal,
        final HttpServletRequest request,
        final HttpServletResponse response)
        throws Exception {
        
        val result = server.startRegistration(
            request,
            authenticatedPrincipal.getName(),
            Optional.of(displayName),
            Optional.ofNullable(credentialNickname),
            requireResidentKey
                ? ResidentKeyRequirement.REQUIRED
                : ResidentKeyRequirement.DISCOURAGED,
            Optional.ofNullable(sessionTokenBase64).map(Unchecked.function(ByteArray::fromBase64Url)));

        if (result.isRight()) {
            return startResponse(new StartRegistrationResponse(result.right().orElseThrow()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().orElseThrow());
    }

    /**
     * Finish registration and provide response entity.
     *
     * @param responseJson the response json
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_REGISTER + WEBAUTHN_ENDPOINT_FINISH, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Finish registration")
    public ResponseEntity<Object> finishRegistration(
        final HttpServletRequest request,
        @RequestBody
        final String responseJson) throws Exception {
        val result = server.finishRegistration(request, responseJson);
        return finishResponse(result, responseJson);
    }

    /**
     * Start authentication and provide response entity.
     *
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_AUTHENTICATE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start authentication")
    public ResponseEntity<Object> startAuthentication(
        final HttpServletRequest request,
        final Principal authenticatedPrincipal) throws Exception {

        val result = server.startAuthentication(request, Optional.ofNullable(authenticatedPrincipal).map(Principal::getName));
        if (result.isRight()) {
            return startResponse(new StartAuthenticationResponse(result.right().orElseThrow()));
        }
        return messagesJson(ResponseEntity.badRequest(), result.left().orElseThrow());
    }

    /**
     * Finish authentication and create response entity.
     *
     * @param responseJson the response json
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = WEBAUTHN_ENDPOINT_AUTHENTICATE + WEBAUTHN_ENDPOINT_FINISH, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Finish authentication")
    public ResponseEntity<Object> finishAuthentication(
        final HttpServletRequest request,
        @RequestBody
        final String responseJson) throws Exception {
        val result = server.finishAuthentication(request, responseJson);
        return finishResponse(result, responseJson);
    }

    @RequiredArgsConstructor
    @Getter
    @SuppressWarnings("UnusedMethod")
    private static final class StartAuthenticationResponse {
        private final boolean success = true;

        private final AssertionRequestWrapper request;

        private final StartAuthenticationActions actions = new StartAuthenticationActions();
    }

    @RequiredArgsConstructor
    @Getter
    @SuppressWarnings("UnusedMethod")
    private static final class StartRegistrationResponse {
        private final boolean success = true;

        private final RegistrationRequest request;

        private final StartRegistrationActions actions = new StartRegistrationActions();
    }

    @Getter
    @SuppressWarnings("UnusedMethod")
    private static final class StartRegistrationActions {
        private final String finish = BASE_ENDPOINT_WEBAUTHN + WEBAUTHN_ENDPOINT_REGISTER + WEBAUTHN_ENDPOINT_FINISH;
    }

    @Getter
    @SuppressWarnings("UnusedMethod")
    private static final class StartAuthenticationActions {
        private final String finish = BASE_ENDPOINT_WEBAUTHN + WEBAUTHN_ENDPOINT_AUTHENTICATE + WEBAUTHN_ENDPOINT_FINISH;
    }
}
