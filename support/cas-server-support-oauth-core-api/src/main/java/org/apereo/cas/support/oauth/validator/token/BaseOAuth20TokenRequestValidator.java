package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.core.Ordered;

/**
 * This is {@link BaseOAuth20TokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public abstract class BaseOAuth20TokenRequestValidator implements OAuth20TokenRequestValidator {
    private final OAuth20ConfigurationContext configurationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Check the grant type against expected grant types.
     *
     * @param type          the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
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

    /**
     * Is grant type supported.
     *
     * @param registeredService the registered service
     * @param type              the type
     * @return true/false
     */
    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService, final OAuth20GrantTypes type) {
        return isGrantTypeSupportedBy(registeredService, type.getType());
    }

    /**
     * Is grant type supported service.
     *
     * @param registeredService the registered service
     * @param type              the type
     * @return true/false
     */
    protected boolean isGrantTypeSupportedBy(final OAuthRegisteredService registeredService, final String type) {
        return OAuth20Utils.isAuthorizedGrantTypeForService(type, registeredService);
    }

    @Override
    public boolean validate(final JEEContext ctx) {
        val request = ctx.getNativeRequest();
        val response = ctx.getNativeResponse();

        val grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        val context = new JEEContext(request, response, getConfigurationContext().getSessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());
        val profile = manager.get(true);
        if (profile.isEmpty()) {
            LOGGER.warn("Could not locate authenticated profile for this request. Request is not authenticated");
            return false;
        }

        val uProfile = profile.get();
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
    protected boolean validateInternal(final JEEContext context,
                                       final String grantType,
                                       final ProfileManager manager,
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
    public boolean supports(final JEEContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType.map(String::valueOf).orElse(StringUtils.EMPTY), getGrantType());
    }
}
