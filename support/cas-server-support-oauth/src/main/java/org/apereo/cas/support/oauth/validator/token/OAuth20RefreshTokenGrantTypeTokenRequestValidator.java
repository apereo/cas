package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20RefreshTokenGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    private final TicketRegistry ticketRegistry;

    public OAuth20RefreshTokenGrantTypeTokenRequestValidator(final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                             final TicketRegistry ticketRegistry) {
        super(registeredServiceAccessStrategyEnforcer);
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        val request = context.getRequest();
        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.REFRESH_TOKEN)
            || !HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_ID)
            || !HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_SECRET)) {
            return false;
        }
        val token = request.getParameter(OAuth20Constants.REFRESH_TOKEN);
        val refreshToken = ticketRegistry.getTicket(token);
        if (refreshToken == null) {
            LOGGER.warn("Provided refresh token [{}] cannot be found in the registry", token);
            return false;
        }
        if (!RefreshToken.class.isAssignableFrom(refreshToken.getClass())) {
            LOGGER.warn("Provided refresh token [{}] is found in the registry but its type is not classified as a refresh token", token);
            return false;
        }
        if (refreshToken.isExpired()) {
            LOGGER.warn("Provided refresh token [{}] has expired and is no longer valid.", token);
            return false;
        }
        return true;
    }
}
