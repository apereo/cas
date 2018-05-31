package org.apereo.cas.support.oauth.validator.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.Pac4jUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

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

    @Override
    public boolean validate(final J2EContext context) {
        final var request = context.getRequest();
        final var response = context.getResponse();

        final var grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        final var manager = Pac4jUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            LOGGER.warn("Could not locate authenticated profile for this request");
            return false;
        }

        final var uProfile = profile.get();
        if (uProfile == null) {
            LOGGER.warn("Could not locate authenticated profile for this request as null");
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
        final var grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    /**
     * Check the grant type against expected grant types.
     *
     * @param type          the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
    private static boolean isGrantTypeSupported(final String type, final OAuth20GrantTypes... expectedTypes) {
        LOGGER.debug("Grant type received: [{}]", type);
        for (final var expectedType : expectedTypes) {
            if (OAuth20Utils.isGrantType(type, expectedType)) {
                return true;
            }
        }
        LOGGER.error("Unsupported grant type: [{}]", type);
        return false;
    }
}
