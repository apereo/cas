package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.heimdall.authzen.AuthZenEvaluationsRequest;
import org.apereo.cas.heimdall.authzen.AuthZenEvaluationsResponse;
import org.apereo.cas.heimdall.authzen.AuthZenEvaluationsSemantic;
import org.apereo.cas.heimdall.authzen.AuthZenResponse;
import org.apereo.cas.heimdall.engine.AuthorizationEngine;
import org.apereo.cas.heimdall.engine.AuthorizationPrincipalParser;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ProofOfPossessionValidator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPNonceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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

    /**
     * The AuthZEN access evaluation path, relative to {@link #BASE_URL}.
     */
    public static final String AUTHZEN_PATH = "/authzen";

    /**
     * The AuthZEN access evaluations path, relative to {@link #BASE_URL}.
     */
    public static final String AUTHZEN_EVALUATIONS_PATH = AUTHZEN_PATH + "/evaluations";

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    private static final Set<String> PROTOCOL_HEADERS = Set.of("accept", "accept-encoding", "connection", "content-length",
        "content-type", "dpop", "host", "keep-alive", "transfer-encoding", "upgrade");

    private final AuthorizationEngine authorizationEngine;
    private final AuthorizationPrincipalParser principalParser;
    private final ObjectProvider<OAuth20ProofOfPossessionValidator> proofOfPossessionValidator;

    /**
     * AuthZen access evaluation API.
     *
     * @param authorizationRequest the authorization request
     * @param request              the request
     * @param response             the response
     * @return the response entity
     */
    @PostMapping(AUTHZEN_PATH)
    @Operation(summary = "Authorize request via OpenID AuthZEN API",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "AuthZenRequest JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthorizationRequest.class)
            )
        ))
    public ResponseEntity authzen(
        @RequestBody final @Valid AuthorizationRequest authorizationRequest,
        final HttpServletRequest request, final HttpServletResponse response) {

        echoRequestId(request, response);
        try {
            validateAuthZenRequest(authorizationRequest);
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        var requestToAuthorize = AuthorizationRequest.builder().build();
        try {
            requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, request, response);
        } catch (final Throwable e) {
            LOGGER.debug("AuthZEN caller authentication failed", e);
            return unauthenticated(e);
        }
        try {
            requestToAuthorize.log();
            return ResponseEntity.ok(toAuthZenResponse(authorizationEngine.authorize(requestToAuthorize)));
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * AuthZEN access evaluations API: evaluates several requests, sharing top-level defaults, with one
     * caller authentication. Without evaluations, the request behaves as a single access evaluation.
     *
     * @param evaluationsRequest the evaluations request
     * @param request            the request
     * @param response           the response
     * @return the response entity
     */
    @PostMapping(AUTHZEN_EVALUATIONS_PATH)
    @Operation(summary = "Authorize a batch of requests via OpenID AuthZEN API",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "AuthZEN access evaluations JSON payload",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthZenEvaluationsRequest.class)
            )
        ))
    public ResponseEntity evaluations(
        @RequestBody final AuthZenEvaluationsRequest evaluationsRequest,
        final HttpServletRequest request, final HttpServletResponse response) {
        if (evaluationsRequest.getEvaluations().isEmpty()) {
            return authzen(evaluationsRequest.toAuthorizationRequest(), request, response);
        }
        echoRequestId(request, response);
        try {
            val authorizationHeader = Objects.requireNonNull(request.getHeader(HttpHeaders.AUTHORIZATION));
            Assert.hasText(authorizationHeader, "Authorization header cannot be blank");
            principalParser.authenticateCaller(authorizationHeader, new JEEContext(request, response));
        } catch (final Throwable e) {
            LOGGER.debug("AuthZEN caller authentication failed", e);
            return unauthenticated(e);
        }
        val semantic = evaluationsRequest.getEvaluationsSemantic();
        val decisions = new ArrayList<AuthZenResponse>();
        for (val authorizationRequest : evaluationsRequest.toAuthorizationRequests()) {
            val decision = evaluate(authorizationRequest);
            decisions.add(decision);
            if ((semantic == AuthZenEvaluationsSemantic.DENY_ON_FIRST_DENY && !decision.isDecision())
                || (semantic == AuthZenEvaluationsSemantic.PERMIT_ON_FIRST_PERMIT && decision.isDecision())) {
                break;
            }
        }
        return ResponseEntity.ok(new AuthZenEvaluationsResponse(decisions));
    }

    /**
     * Authorize response entity.
     *
     * @param authorizationRequest the authorization request
     * @param request              the request
     * @param response             the response
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
        final HttpServletRequest request, final HttpServletResponse response) {

        if (authorizationRequest.getSubject() != null || authorizationRequest.getResource() != null || authorizationRequest.getAction() != null) {
            val body = Map.of("message", "Subject, resource and action belong to AuthZEN requests; use %s/authzen".formatted(BASE_URL));
            return ResponseEntity.badRequest().body(body);
        }
        var requestToAuthorize = AuthorizationRequest.builder().build();
        try {
            Assert.notNull(authorizationRequest.getMethod(), "Method cannot be null");
            Assert.notNull(authorizationRequest.getUri(), "URI cannot be null");
            Assert.notNull(authorizationRequest.getNamespace(), "Namespace cannot be null");
            Assert.notNull(authorizationRequest.getContext(), "Context cannot be null");
        } catch (final IllegalArgumentException e) {
            LoggingUtils.error(LOGGER, e);
            return buildResponse(AuthorizationResponse.unauthorized(e.getMessage()));
        }
        try {
            requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, request, response);
        } catch (final Throwable e) {
            LOGGER.debug("Heimdall caller authentication failed", e);
            return unauthenticated(e);
        }
        try {
            requestToAuthorize.log();
            val authorizationResponse = authorizationEngine.authorize(requestToAuthorize);
            return buildResponse(authorizationResponse);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return buildResponse(AuthorizationResponse.unauthorized(e.getMessage()));
        }
    }

    private AuthZenResponse evaluate(final AuthorizationRequest authorizationRequest) {
        try {
            validateAuthZenRequest(authorizationRequest);
        } catch (final IllegalArgumentException e) {
            return failedEvaluation(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        try {
            val principal = principalParser.resolveSubject(authorizationRequest.getSubject());
            val requestToAuthorize = authorizationRequest.withPrincipal(principal);
            requestToAuthorize.log();
            return toAuthZenResponse(authorizationEngine.authorize(requestToAuthorize));
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return failedEvaluation(HttpStatus.INTERNAL_SERVER_ERROR, "Evaluation failed");
        }
    }

    private static AuthZenResponse toAuthZenResponse(final AuthorizationResponse authorizationResponse) {
        val builder = AuthZenResponse.builder().decision(authorizationResponse.getDecision());
        if (!authorizationResponse.getDecision() && authorizationResponse.getReason() != null) {
            builder.context(Map.of("reason", authorizationResponse.getReason().getCode()));
        }
        return builder.build();
    }

    private static AuthZenResponse failedEvaluation(final HttpStatus status, final String message) {
        return AuthZenResponse.builder()
            .decision(false)
            .context(Map.of("error", Map.of("status", status.value(), "message", message)))
            .build();
    }

    private static void validateAuthZenRequest(final AuthorizationRequest authorizationRequest) {
        Assert.notNull(authorizationRequest.getSubject(), "Subject is required");
        Assert.hasText(authorizationRequest.getSubject().getId(), "Subject id is required");
        Assert.hasText(authorizationRequest.getSubject().getType(), "Subject type is required");
        Assert.notNull(authorizationRequest.getAction(), "Action is required");
        Assert.hasText(authorizationRequest.getAction().getName(), "Action name is required");
        Assert.notNull(authorizationRequest.getResource(), "Resource is required");
        Assert.hasText(authorizationRequest.getResource().getId(), "Resource id is required");
        Assert.hasText(authorizationRequest.getResource().getType(), "Resource type is required");
        Assert.notNull(authorizationRequest.getContext(), "Context must be an object");
    }

    private static void echoRequestId(final HttpServletRequest request, final HttpServletResponse response) {
        val requestId = request.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.isNotBlank(requestId)) {
            response.setHeader(REQUEST_ID_HEADER, requestId);
        }
    }

    /**
     * The answer to a caller that failed to authenticate. A caller whose DPoP proof lacks the nonce CAS requires is asked
     * for it with {@code use_dpop_nonce}, the fresh nonce being already in the {@code DPoP-Nonce} header of the response.
     * The {@code DPoP} challenge lists the algorithms accepted for DPoP proofs (RFC 9449, section 7.1).
     *
     * @param e the authentication failure
     * @return the response entity
     */
    private ResponseEntity unauthenticated(final Throwable e) {
        final Set<JWSAlgorithm> algorithms = Optional.ofNullable(proofOfPossessionValidator.getIfAvailable())
            .map(OAuth20ProofOfPossessionValidator::getAcceptedSigningAlgorithms)
            .orElseGet(Set::of);
        if (e instanceof InvalidDPoPNonceException) {
            return OAuth20Utils.useDPoPNonceResponse(algorithms);
        }
        val dpopChallenge = OAuth20Utils.toDPoPAlgorithmsChallengeParameter(algorithms)
            .map(algs -> OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + algs)
            .orElse(OAuth20Constants.TOKEN_TYPE_DPOP);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer", dpopChallenge, "Basic realm=\"Heimdall\"")
            .build();
    }

    private AuthorizationRequest prepareAuthorizationRequest(final AuthorizationRequest authorizationRequest,
                                                             final HttpServletRequest request,
                                                             final HttpServletResponse response)
            throws Throwable {
        val authorizationHeader = Objects.requireNonNull(request.getHeader(HttpHeaders.AUTHORIZATION));
        Assert.hasText(authorizationHeader, "Authorization header cannot be blank");
        val principal = principalParser.parse(authorizationHeader, authorizationRequest, new JEEContext(request, response));
        val requestToAuthorize = authorizationRequest.withPrincipal(principal);
        if (!requestToAuthorize.isAuthZen()) {
            val context = (Map<String, Object>) requestToAuthorize.getContext();
            HttpRequestUtils.getRequestHeaders(request).forEach((name, value) -> {
                if (!PROTOCOL_HEADERS.contains(name.toLowerCase(Locale.ENGLISH))) {
                    context.putIfAbsent(name, value);
                }
            });
        }
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

