package org.apereo.cas.oidc.federation.registration;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.web.OidcRequestObjectDecrypter;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs ahead of the OAuth/OIDC interceptors on the authorization and pushed authorization endpoints, so that a relying
 * party of the federation is registered automatically (OpenID Federation 1.0, section 12.1) before client
 * authentication and the authorization request validators look the client up.
 * <p>
 * An authorization request of an automatically registered client must prove that the client controls its keys
 * (section 12.1.1): it carries a request object signed with a key of the relying party's resolved metadata, whose
 * {@code iss} and {@code client_id} are the Entity Identifier, whose {@code aud} is only the OP's Entity Identifier,
 * with {@code jti} and {@code exp} and without {@code sub}. A request object is used once: its {@code jti} is kept
 * in the ticket registry for the lifetime of transient session tickets, and only the very same request object may come
 * back in that time, which is what the authorization endpoint receives again after the login. A pushed authorization
 * request proves the keys through client authentication instead, which the pushed authorization endpoint requires of an
 * automatically registered client since its registration only allows asymmetric methods, so the authorization request
 * that refers to it by {@code request_uri} carries no request object. A request object encrypted to CAS is decrypted
 * first and its nested JWT checked as above, while replay is tracked on the request object as sent. Requests that fail
 * are refused with {@code invalid_request} and are never redirected to the client, since its trust could not be
 * established.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationAutomaticRegistrationInterceptor implements HandlerInterceptor {
    private static final String REQUEST_OBJECT_TICKET_PREFIX = "OidcFederationRequestObject:";

    private static final String REQUEST_OBJECT_DIGEST = "requestObjectDigest";

    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 30;

    private final OidcFederationAutomaticClientRegistration clientRegistration;

    private final OidcIssuerService issuerService;

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    private final ApplicationContext applicationContext;

    private final ObjectProvider<OidcRequestObjectDecrypter> requestObjectDecrypter;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler)
            throws Exception {
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        if (!isAuthorizationRequest(request) || !OidcFederationAutomaticClientRegistration.isFederatedEntityId(clientId)) {
            return true;
        }
        val registeredService = clientRegistration.register(clientId);
        if (registeredService instanceof final OidcRegisteredService oidcService
            && OidcFederationAutomaticClientRegistration.isAutomaticallyRegistered(oidcService)
            && !isPushedAuthorizationRequest(request)
            && StringUtils.isBlank(request.getParameter(OidcConstants.REQUEST_URI))) {
            val error = verifyRequestObject(request, oidcService);
            if (error.isPresent()) {
                LOGGER.warn("Refusing the authorization request of [{}]: [{}]", clientId, error.get());
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(OAuth20Utils.toJson(
                    OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, error.get())));
                return false;
            }
        }
        return true;
    }

    /**
     * Verify the request object of an authorization request sent by an automatically registered client.
     *
     * @param request           the request
     * @param registeredService the automatically registered service
     * @return the reason the request is refused, or empty when the request object proves the client's keys
     */
    protected Optional<String> verifyRequestObject(final HttpServletRequest request, final OidcRegisteredService registeredService) {
        val requestObject = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isBlank(requestObject)) {
            return Optional.of("Automatic registration requires a signed request object or a pushed authorization request");
        }
        try {
            val nestedRequestObject = decryptRequestObject(requestObject);
            val jwtId = JWTParser.parse(nestedRequestObject).getJWTClaimsSet().getJWTID();
            if (StringUtils.isBlank(jwtId)) {
                return Optional.of("Request object has no jti");
            }
            val clientId = registeredService.getClientId();
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(
                REQUEST_OBJECT_TICKET_PREFIX + clientId + ':' + DigestUtils.sha256(jwtId));
            val digest = DigestUtils.sha256(requestObject);
            val usedTicket = findUsedRequestObject(ticketId);
            if (usedTicket != null) {
                return Strings.CS.equals(digest, usedTicket.getPropertyAsString(REQUEST_OBJECT_DIGEST))
                    ? Optional.empty()
                    : Optional.of("Request object jti has already been used");
            }
            val error = verifyRequestObjectClaims(nestedRequestObject, registeredService);
            if (error.isEmpty()) {
                val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                val properties = new HashMap<String, Serializable>();
                properties.put(OAuth20Constants.CLIENT_ID, clientId);
                properties.put(REQUEST_OBJECT_DIGEST, digest);
                ticketRegistry.addTicket(factory.create(ticketId, properties));
            }
            return error;
        } catch (final Throwable e) {
            LOGGER.debug("Unable to verify the request object of [{}]: [{}]", registeredService.getClientId(), e.getMessage());
            return Optional.of("Request object is invalid");
        }
    }

    private String decryptRequestObject(final String requestObject) {
        if (!OidcRequestObjectDecrypter.isEncrypted(requestObject)) {
            return requestObject;
        }
        val decrypter = requestObjectDecrypter.getIfAvailable();
        if (decrypter == null) {
            throw new IllegalStateException("Encrypted request objects cannot be decrypted without OpenID Connect");
        }
        return decrypter.decrypt(requestObject);
    }

    private Optional<String> verifyRequestObjectClaims(final String requestObject, final OidcRegisteredService registeredService) {
        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING))
            .map(JsonWebKeySet::getJsonWebKeys)
            .orElseGet(List::of);
        if (keys.isEmpty()) {
            return Optional.of("Relying party metadata has no keys to verify the request object");
        }
        val clientId = registeredService.getClientId();
        val entityId = issuerService.determineIssuer(Optional.of(registeredService));
        val consumer = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new JwksVerificationKeyResolver(keys))
            .setExpectedIssuer(true, clientId)
            .setExpectedAudience(true, entityId)
            .setRequireJwtId()
            .setRequireExpirationTime()
            .setAllowedClockSkewInSeconds(ALLOWED_CLOCK_SKEW_SECONDS)
            .build();
        try {
            val claims = consumer.processToClaims(requestObject);
            if (claims.getAudience().size() != 1) {
                return Optional.of("Request object aud must only be the OP Entity Identifier");
            }
            if (!Strings.CS.equals(clientId, claims.getClaimValueAsString(OAuth20Constants.CLIENT_ID))) {
                return Optional.of("Request object client_id must be the Entity Identifier of the client");
            }
            if (claims.hasClaim("sub")) {
                return Optional.of("Request object must not carry sub");
            }
            return Optional.empty();
        } catch (final InvalidJwtException | MalformedClaimException e) {
            LOGGER.debug("Request object of [{}] failed verification: [{}]", clientId, e.getMessage());
            return Optional.of("Request object signature or claims are invalid");
        }
    }

    private @Nullable TransientSessionTicket findUsedRequestObject(final String ticketId) {
        try {
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            return ticket != null && !ticket.isExpired() ? ticket : null;
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Request object [{}] has not been used", ticketId);
            return null;
        }
    }

    private static boolean isAuthorizationRequest(final HttpServletRequest request) {
        return isPushedAuthorizationRequest(request)
            || requestPathEndsWith(request, OidcConstants.AUTHORIZE_URL)
            || requestPathEndsWith(request, OAuth20Constants.AUTHORIZE_URL);
    }

    private static boolean isPushedAuthorizationRequest(final HttpServletRequest request) {
        return requestPathEndsWith(request, OidcConstants.PUSHED_AUTHORIZE_URL);
    }

    private static boolean requestPathEndsWith(final HttpServletRequest request, final String endpoint) {
        return Strings.CS.endsWith(Strings.CS.removeEnd(request.getRequestURI(), "/"), '/' + endpoint);
    }
}
