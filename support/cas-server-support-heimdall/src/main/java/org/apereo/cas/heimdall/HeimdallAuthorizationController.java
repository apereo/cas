package org.apereo.cas.heimdall;

import org.apereo.cas.heimdall.engine.AuthorizationEngine;
import org.apereo.cas.heimdall.engine.AuthorizationPrincipalParser;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link HeimdallAuthorizationController}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RestController
@RequestMapping(value = HeimdallAuthorizationController.BASE_URL,
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class HeimdallAuthorizationController {

    /**
     * The base url for this controller.
     */
    public static final String BASE_URL = "/heimdall";

    private final AuthorizationEngine authorizationEngine;
    private final AuthorizationPrincipalParser principalParser;

    /**
     * Authorize response entity.
     *
     * @param authorizationRequest the authorization request
     * @param request              the request
     * @return the response entity
     */
    @PostMapping("/authorize")
    @Operation(summary = "Authorize request via Heimdall",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "AuthorizationRequest JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthorizationRequest.class)
            )
        ))
    public ResponseEntity authorize(
        @RequestBody final @Valid AuthorizationRequest authorizationRequest,
        final HttpServletRequest request) {

        try {
            val requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, request);
            logRequest(requestToAuthorize);
            val authorizationResponse = authorizationEngine.authorize(requestToAuthorize);
            return buildResponse(authorizationResponse);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return buildResponse(AuthorizationResponse.unauthorized(e.getMessage()));
        }
    }

    private AuthorizationRequest prepareAuthorizationRequest(final AuthorizationRequest authorizationRequest,
                                                             final HttpServletRequest request) throws Throwable {
        val authorizationHeader = Objects.requireNonNull(request.getHeader(HttpHeaders.AUTHORIZATION));
        Assert.hasText(authorizationHeader, "Authorization header cannot be blank");
        val principal = principalParser.parse(authorizationHeader);
        val headers = HttpRequestUtils.getRequestHeaders(request);
        val requestToAuthorize = authorizationRequest.withPrincipal(principal);
        requestToAuthorize.getContext().putAll((Map) headers);
        return requestToAuthorize;
    }

    protected ResponseEntity<AuthorizationResponse> buildResponse(
        final AuthorizationResponse authorizationResponse) {
        logRequest(authorizationResponse);
        return ResponseEntity
            .status(authorizationResponse.getStatus())
            .body(authorizationResponse);
    }
    
    private static void logRequest(final BaseHeimdallRequest heimdallRequest) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(heimdallRequest.toJson());
        }
    }
}
