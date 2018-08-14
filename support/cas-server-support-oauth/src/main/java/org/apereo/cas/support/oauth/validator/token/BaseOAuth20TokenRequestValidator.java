package org.apereo.cas.support.oauth.validator.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.Pac4jUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link BaseOAuth20TokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseOAuth20TokenRequestValidator implements OAuth20TokenRequestValidator {
    /**
     * Access strategy enforcer.
     */
    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Get registered service for request context.
     *
     * @param context request context
     * @param uProfile the profile
     * @return a registered service for given context
     */
    protected OAuthRegisteredService getRegisteredService(final J2EContext context, final UserProfile uProfile) {
        final HttpServletRequest request = context.getRequest();
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
    }

    @Override
    public boolean validate(final J2EContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final ProfileManager manager = Pac4jUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            LOGGER.warn("Could not locate authenticated profile for this request");
            return false;
        }

        final UserProfile uProfile = profile.orElse(null);
        if (uProfile == null) {
            LOGGER.warn("Could not locate authenticated profile for this request as null");
            return false;
        }

        final String grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        if (!OAuth20Utils.isAuthorizedGrantTypeForService(context, getRegisteredService(context, uProfile))) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        return validateInternal(context, grantType, manager, uProfile);
    }

    /**
     * Validate internal.
     *
     * @param context     the context
     * @param grantType   the grant type
     * @param manager     the manager
     * @param userProfile the profile
     * @return true/false
     */
    protected boolean validateInternal(final J2EContext context, final String grantType, final ProfileManager manager,
                                       final UserProfile userProfile) {
        return false;
    }

    /**
     * Gets grant type.
     *
     * @return the grant type
     */
    protected abstract OAuth20GrantTypes getGrantType();

    @Override
    public boolean supports(final J2EContext context) {
        final String grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }
}
