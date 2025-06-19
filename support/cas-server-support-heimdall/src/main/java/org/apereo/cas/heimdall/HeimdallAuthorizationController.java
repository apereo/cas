package org.apereo.cas.heimdall;

import org.apereo.cas.heimdall.engine.AuthorizationEngine;
import org.apereo.cas.heimdall.engine.AuthorizationPrincipalParser;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authorization")
public class HeimdallAuthorizationController {

    /**
     * The base url for this controller.
     */
    public static final String BASE_URL = "/heimdall";

    private final AuthorizationEngine authorizationEngine;
    private final AuthorizationPrincipalParser principalParser;

    /**
     * AuthZen access evaluation API.
     *
     * @param authorizationRequest the authorization request
     * @param request              the request
     * @return the response entity
     */
    @PostMapping("/authzen")
    @Operation(summary = "Authorize request via OpenID Connect AuthZen API",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "AuthZenRequest JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthorizationRequest.class)
            )
        ))
    public ResponseEntity authzen(
        @RequestBody
        final @Valid AuthorizationRequest authorizationRequest,
        final HttpServletRequest request) {

        try {
            Assert.notNull(authorizationRequest.getSubject(), "Method cannot be null");
            Assert.notNull(authorizationRequest.getAction(), "URI cannot be null");
            Assert.notNull(authorizationRequest.getResource(), "Namespace cannot be null");
            Assert.notNull(authorizationRequest.getContext(), "Context cannot be null");
            
            val requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, request);
            requestToAuthorize.log();
            val authorizationResponse = authorizationEngine.authorize(requestToAuthorize);
            return buildResponse(authorizationResponse);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return buildResponse(AuthorizationResponse.unauthorized(e.getMessage()));
        }
    }


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
        @RequestBody
        final @Valid AuthorizationRequest authorizationRequest,
        final HttpServletRequest request) {

        try {
            Assert.notNull(authorizationRequest.getMethod(), "Method cannot be null");
            Assert.notNull(authorizationRequest.getUri(), "URI cannot be null");
            Assert.notNull(authorizationRequest.getNamespace(), "Namespace cannot be null");
            Assert.notNull(authorizationRequest.getContext(), "Context cannot be null");

            val requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, request);
            requestToAuthorize.log();
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
        val principal = principalParser.parse(authorizationHeader, authorizationRequest);
        val headers = HttpRequestUtils.getRequestHeaders(request);
        val requestToAuthorize = authorizationRequest.withPrincipal(principal);
        requestToAuthorize.getContext().putAll((Map) headers);
        return requestToAuthorize;
    }

    protected ResponseEntity<AuthorizationResponse> buildResponse(
        final AuthorizationResponse authorizationResponse) {
        authorizationResponse.log();
        return ResponseEntity
            .status(authorizationResponse.getStatus())
            .body(authorizationResponse);
    }
}

