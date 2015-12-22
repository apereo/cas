package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.Pair;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultAccessTokenGenerator}. Creates access tokens
 * by appending the ticket granting ticket id to the request service,
 * and decodes requests as such. The separator between the id
 * and the service is considered to be {@link org.pac4j.core.profile.UserProfile#SEPARATOR}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAccessTokenGenerator")
public final class DefaultAccessTokenGenerator implements AccessTokenGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAccessTokenGenerator.class);

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Override
    public String generate(final Service service, final TicketGrantingTicket ticketGrantingTicket) {
        final String accessToken = ticketGrantingTicket.getId() + UserProfile.SEPARATOR + service.getId();
        LOGGER.debug("Created access token {}, now encoding it as base64", accessToken);
        return CompressionUtils.encodeBase64(accessToken.getBytes());
    }

    @Override
    public Pair<String, Service> degenerate(final String accessTokenInput) {
        final String accessTokenEncoded = new String(CompressionUtils.decodeBase64(accessTokenInput));
        final String[] token = accessTokenEncoded.split(UserProfile.SEPARATOR);
        if (token.length == 2) {
            return new Pair(token[0], this.webApplicationServiceFactory.createService(token[1]));
        }
        throw new IllegalArgumentException("Access token received must include both the id and the requesting service");
    }

}
