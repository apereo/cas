package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.CollectionUtils;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseOAuth20TokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class BaseOAuth20TokenRequestValidator<T extends OAuth20ConfigurationContext> implements OAuth20TokenRequestValidator {
    private final ObjectProvider<T> configurationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    
    private static boolean isGrantTypeSupported(final String type, final OAuth20GrantTypes... expectedTypes) {
        LOGGER.debug("Grant type received: [{}]", type);
        for (val expectedType : expectedTypes) {
            if (OAuth20Utils.isGrantType(type, expectedType)) {
                return true;
            }
        }
        LOGGER.error("Unsupported grant type: [{}]", type);
        return false;
    }

    @Override
    public boolean validate(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        var configurationContextObject = configurationContext.getObject();
        val grantType = configurationContextObject.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        val profile = extractUserProfile(request, response);
        if (profile.isEmpty()) {
            LOGGER.warn("Could not locate authenticated profile for this request. Request is not authenticated");
            return false;
        }
        if (!validateClientSecretInRequestIfAny(request)) {
            LOGGER.warn("Cannot accept [{}] as a query parameter in the request", OAuth20Constants.CLIENT_SECRET);
            return false;
        }
        val userProfile = profile.get();
        configurationContextObject.getProofOfPossessionValidator().validate(request, response);
        return validateInternal(request, response, grantType, userProfile);
    }
    
    /**
     * A grant bound to a DPoP key, such as a code bound with {@code dpop_jkt} or by the DPoP proof of a pushed authorization
     * request (RFC 9449, section 10) or a refresh token issued to a public client (RFC 9449, section 5), may only be used with a
     * DPoP proof made with that key. The proof itself was verified before, and its key thumbprint recorded on the profile.
     * The grant is not redeemed when the keys do not match.
     *
     * @param token         the code or token presented as the grant
     * @param attributeName the authentication attribute of the token that names the bound key
     * @param userProfile   the authenticated profile of the request
     * @throws InvalidDPoPProofException when the request carries no DPoP proof, or one made with another key
     */
    protected void verifyBoundProofOfPossessionKey(final OAuth20Token token, final String attributeName,
                                                   final OAuth20UserProfile userProfile)
            throws InvalidDPoPProofException {
        val boundKey = Optional.ofNullable(token.getAuthentication())
            .flatMap(authentication -> CollectionUtils.firstElement(authentication.getAttributes().get(attributeName)));
        if (boundKey.isPresent()) {
            val presentedKey = Optional.ofNullable(userProfile.getAttribute(OAuth20Constants.DPOP_CONFIRMATION));
            if (presentedKey.isEmpty() || !boundKey.get().toString().equals(presentedKey.get().toString())) {
                throw new InvalidDPoPProofException("The grant is bound to a DPoP key that the request does not prove possession of");
            }
        }
    }

    protected Optional<OAuth20UserProfile> extractUserProfile(final HttpServletRequest request, final HttpServletResponse response) {
        return OAuth20Utils.findAuthenticatedUserProfile(request, response, getConfigurationContext().getObject().getSessionStore());
    }

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val grantType = configurationContext.getObject().getRequestParameterResolver().resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType.map(String::valueOf).orElse(StringUtils.EMPTY), getGrantType());
    }

    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService, final String type) {
        return isGrantTypeSupportedBy(registeredService, type, false);
    }

    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService,
                                             final String type, final boolean rejectUndefined) {
        return OAuth20RequestParameterResolver.isAuthorizedGrantTypeForService(type, registeredService, rejectUndefined);
    }

    protected boolean validateInternal(final HttpServletRequest request,
                                       final HttpServletResponse response,
                                       final String grantType,
                                       final OAuth20UserProfile userProfile)
            throws Throwable {
        return false;
    }

    protected abstract OAuth20GrantTypes getGrantType();

    protected static @Nullable Authentication resolveAuthenticationFrom(final OAuth20Code oauthCode) {
        return oauthCode.isStateless()
            ? oauthCode.getAuthentication()
            : ((AuthenticationAwareTicket) oauthCode.getTicketGrantingTicket()).getAuthentication();
    }

    protected boolean validateClientSecretInRequestIfAny(final HttpServletRequest request) {
        val requestParameterResolver = getConfigurationContext().getObject().getRequestParameterResolver();
        val httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase(Locale.ROOT));
        return httpMethod.equals(HttpMethod.POST) || !requestParameterResolver.isParameterOnQueryString(request, OAuth20Constants.CLIENT_SECRET);
    }


}
