package org.apereo.cas.heimdall;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.heimdall.engine.AuthorizationEngine;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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
    private final TicketRegistry ticketRegistry;

    /**
     * Authorize response entity.
     *
     * @param authorizationRequest the authorization request
     * @param authorizationHeader  the authorization header
     * @param request              the request
     * @return the response entity
     */
    @PostMapping("/authorize")
    public ResponseEntity authorize(
        @RequestBody final @Valid AuthorizationRequest authorizationRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader,
        final HttpServletRequest request) {

        try {
            val requestToAuthorize = prepareAuthorizationRequest(authorizationRequest, authorizationHeader, request);
            logRequest(requestToAuthorize);
            val authorizationResponse = authorizationEngine.authorize(requestToAuthorize);
            logRequest(authorizationResponse);
            return buildResponse(authorizationResponse);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return buildResponse(AuthorizationResponse.unauthorized(e.getMessage()));
        }
    }

    private AuthorizationRequest prepareAuthorizationRequest(final AuthorizationRequest authorizationRequest,
                                                             final String authorizationHeader,
                                                             final HttpServletRequest request) throws Throwable {
        val claims = parseAuthorizationHeader(authorizationHeader);
        val headers = HttpRequestUtils.getRequestHeaders(request);
        val principal = PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal(claims.getSubject(), (Map) claims.getClaims());
        val requestToAuthorize = authorizationRequest.withPrincipal(principal);
        requestToAuthorize.getContext().putAll((Map) headers);
        return requestToAuthorize;
    }

    protected ResponseEntity<AuthorizationResponse> buildResponse(
        final AuthorizationResponse authorizationResponse) {
        return ResponseEntity
            .status(authorizationResponse.getStatus())
            .body(authorizationResponse);
    }

    private JWTClaimsSet parseAuthorizationHeader(final String authorizationHeader) throws Throwable {
        val token = StringUtils.removeStart(authorizationHeader, "Bearer ");
        try {
            return JwtBuilder.parse(token);
        } catch (final Exception e) {
            val ticket = ticketRegistry.getTicket(token, OAuth20AccessToken.class);
            FunctionUtils.throwIf(ticket == null || ticket.isExpired(), AuthenticationException::new);
            val claimsMap = new HashMap<String, Object>(ticket.getClaims());
            val authentication = ticket.getAuthentication();
            claimsMap.putAll(authentication.getAttributes());
            claimsMap.putAll(authentication.getPrincipal().getAttributes());
            claimsMap.put(OAuth20Constants.SCOPE, ticket.getScopes());
            claimsMap.put(OAuth20Constants.TOKEN, token);
            claimsMap.put(OAuth20Constants.CLAIM_SUB, authentication.getPrincipal().getId());
            return JWTClaimsSet.parse(claimsMap);
        }
    }

    private static void logRequest(final BaseHeimdallRequest heimdallRequest) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(heimdallRequest.toJson());
        }
    }
}
