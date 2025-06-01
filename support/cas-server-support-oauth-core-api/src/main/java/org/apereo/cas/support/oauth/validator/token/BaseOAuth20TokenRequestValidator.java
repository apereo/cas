package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.code.OAuth20Code;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import java.util.Locale;

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
    public boolean validate(final WebContext context) throws Throwable {
        val grantType = configurationContext.getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        val manager = new ProfileManager(context, getConfigurationContext().getObject().getSessionStore());
        val profile = manager.getProfile();
        if (profile.isEmpty()) {
            LOGGER.warn("Could not locate authenticated profile for this request. Request is not authenticated");
            return false;
        }
        if (!validateClientSecretInRequestIfAny(context)) {
            LOGGER.warn("Cannot accept [{}] as a query parameter in the request", OAuth20Constants.CLIENT_SECRET);
            return false;
        }
        val userProfile = profile.get();
        return validateInternal(context, grantType, manager, userProfile);
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
                                       final UserProfile userProfile) throws Throwable {
        return false;
    }

    protected abstract OAuth20GrantTypes getGrantType();

    protected static Authentication resolveAuthenticationFrom(final OAuth20Code oauthCode) {
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
