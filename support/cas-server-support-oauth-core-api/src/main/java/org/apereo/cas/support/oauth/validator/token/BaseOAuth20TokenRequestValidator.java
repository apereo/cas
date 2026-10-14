package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
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
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;

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
    public boolean validate(final WebContext webContext) throws Throwable {
        var configurationContextObject = configurationContext.getObject();
        val grantType = configurationContextObject.getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        val manager = new ProfileManager(webContext, getConfigurationContext().getObject().getSessionStore());
        val profile = extractUserProfile(webContext, manager);
        if (profile.isEmpty()) {
            LOGGER.warn("Could not locate authenticated profile for this request. Request is not authenticated");
            return false;
        }
        if (!validateClientSecretInRequestIfAny(webContext)) {
            LOGGER.warn("Cannot accept [{}] as a query parameter in the request", OAuth20Constants.CLIENT_SECRET);
            return false;
        }
        val userProfile = profile.get();
        configurationContextObject.getProofOfPossessionValidator().validate(webContext);
        return validateInternal(webContext, grantType, manager, userProfile);
    }
    
    /**
     * A grant bound to a DPoP key, such as a code bound with {@code dpop_jkt} or by the DPoP proof of a pushed authorization
     * request (RFC 9449, section 10) or a refresh token issued to a public client (RFC 9449, section 5), may only be used with a
     * DPoP proof made with that key. The proof itself was verified before, and its key thumbprint recorded on the profile.
     * The grant is not redeemed when the keys do not match.
     *
     * @param token         the code or token presented as the grant
     * @param attributeName the authentication attribute of the token that names the bound key
     * @param manager       the profile manager
     * @throws InvalidDPoPProofException when the request carries no DPoP proof, or one made with another key
     */
    protected void verifyBoundProofOfPossessionKey(final OAuth20Token token, final String attributeName,
                                                   final ProfileManager manager)
            throws InvalidDPoPProofException {
        val boundKey = Optional.ofNullable(token.getAuthentication())
            .flatMap(authentication -> CollectionUtils.firstElement(authentication.getAttributes().get(attributeName)));
        if (boundKey.isPresent()) {
            val presentedKey = manager.getProfile().map(profile -> profile.getAttribute(OAuth20Constants.DPOP_CONFIRMATION));
            if (presentedKey.isEmpty() || !boundKey.get().toString().equals(presentedKey.get().toString())) {
                throw new InvalidDPoPProofException("The grant is bound to a DPoP key that the request does not prove possession of");
            }
        }
    }

    protected Optional<UserProfile> extractUserProfile(final WebContext context, final ProfileManager manager) {
        return manager.getProfile();
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = configurationContext.getObject().getRequestParameterResolver().resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType.map(String::valueOf).orElse(StringUtils.EMPTY), getGrantType());
    }

    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService, final String type) {
        return isGrantTypeSupportedBy(registeredService, type, false);
    }

    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService,
                                             final String type, final boolean rejectUndefined) {
        return OAuth20RequestParameterResolver.isAuthorizedGrantTypeForService(type, registeredService, rejectUndefined);
    }

    protected boolean validateInternal(final WebContext context,
                                       final String grantType,
                                       final ProfileManager manager,
                                       final UserProfile userProfile)
            throws Throwable {
        return false;
    }

    protected abstract OAuth20GrantTypes getGrantType();

    protected static @Nullable Authentication resolveAuthenticationFrom(final OAuth20Code oauthCode) {
        return oauthCode.isStateless()
            ? oauthCode.getAuthentication()
            : ((AuthenticationAwareTicket) oauthCode.getTicketGrantingTicket()).getAuthentication();
    }

    protected boolean validateClientSecretInRequestIfAny(final WebContext webContext) {
        val requestParameterResolver = getConfigurationContext().getObject().getRequestParameterResolver();
        val httpMethod = HttpMethod.valueOf(webContext.getRequestMethod().toUpperCase(Locale.ROOT));
        return httpMethod.equals(HttpMethod.POST) || !requestParameterResolver.isParameterOnQueryString(webContext, OAuth20Constants.CLIENT_SECRET);
    }


}
