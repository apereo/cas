package org.apereo.cas.support.oauth.web.response.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.util.CommonHelper;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OAuth20AuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20AuthorizationCodeAuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    private final OAuthCodeFactory oAuthCodeFactory;

    @Override
    public View build(final J2EContext context, final String clientId, final AccessTokenRequestDataHolder holder) {
        final var authentication = holder.getAuthentication();
        final var code = oAuthCodeFactory.create(holder.getService(), authentication, holder.getTicketGrantingTicket(), holder.getScopes());
        LOGGER.debug("Generated OAuth code: [{}]", code);
        this.ticketRegistry.addTicket(code);

        final var state = authentication.getAttributes().get(OAuth20Constants.STATE).toString();
        final var nonce = authentication.getAttributes().get(OAuth20Constants.NONCE).toString();

        final var redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);

        var callbackUrl = redirectUri;
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.CODE, code.getId());
        if (StringUtils.isNotBlank(state)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.STATE, state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.NONCE, nonce);
        }
        LOGGER.debug("Redirecting to URL [{}]", callbackUrl);
        return new RedirectView(callbackUrl);
    }

    @Override
    public boolean supports(final J2EContext context) {
        final var responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.CODE.getType());
    }
}
