package org.apereo.cas.support.oauth.web.response.callback;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OAuth20AuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20AuthorizationCodeAuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizationCodeAuthorizationResponseBuilder.class);

    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    private final OAuthCodeFactory oAuthCodeFactory;

    public OAuth20AuthorizationCodeAuthorizationResponseBuilder(final TicketRegistry ticketRegistry, final OAuthCodeFactory oAuthCodeFactory) {
        this.ticketRegistry = ticketRegistry;
        this.oAuthCodeFactory = oAuthCodeFactory;
    }

    @Override
    public View build(final J2EContext context, final String clientId,
                      final AccessTokenRequestDataHolder holder) {
        final OAuthCode code = oAuthCodeFactory.create(holder.getService(), holder.getAuthentication(), holder.getTicketGrantingTicket());
        LOGGER.debug("Generated OAuth code: [{}]", code);
        this.ticketRegistry.addTicket(code);

        final String state = holder.getAuthentication().getAttributes().get(OAuth20Constants.STATE).toString();
        final String nonce = holder.getAuthentication().getAttributes().get(OAuth20Constants.NONCE).toString();

        final String redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);
        
        String callbackUrl = redirectUri;
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
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.CODE.getType());
    }
}
