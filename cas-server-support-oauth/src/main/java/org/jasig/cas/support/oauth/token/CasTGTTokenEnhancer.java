package org.jasig.cas.support.oauth.token;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * Resets the value of the oauth token to the found TGT for that authentication.
 * @author Joe McCall
 *
 */
public class CasTGTTokenEnhancer implements TokenEnhancer {

    private static final Logger log = LoggerFactory.getLogger(CasTGTTokenEnhancer.class);

    @NotNull
    private TokenExpirationConfig tokenExpirationConfig;

    @NotNull
    private TicketRegistry casTicketRegistry;

    public CasTGTTokenEnhancer(final TokenExpirationConfig tokenExpirationConfig, final TicketRegistry casTicketRegistry) {
        this.tokenExpirationConfig = tokenExpirationConfig;
        this.casTicketRegistry = casTicketRegistry;
    }

    @Override
    public OAuth2AccessToken enhance(final OAuth2AccessToken accessToken, final OAuth2Authentication authentication) {
        // Get the user from the authentication
        String casUserName = authentication.getName();

        DefaultOAuth2AccessToken returnAccessToken = null;

        log.debug("There are {} tickets in the ticket registry", casTicketRegistry.getTickets().size());

        // Then find the TGT for that user
        for (Ticket casTicket: casTicketRegistry.getTickets()) {
            log.debug("Checking ticket for value {}", casTicket.getId());
            if (casTicket instanceof TicketGrantingTicket) {
                TicketGrantingTicket casTGT = (TicketGrantingTicket) casTicket;
                if (casUserName.equals(casTGT.getAuthentication().getPrincipal().getId())) {
                    log.debug("Setting the returnAccessToken value to {}", casTGT.getId());
                    log.debug("Setting the exipation to {}", tokenExpirationConfig.getAccessTokenValiditySeconds());
                    returnAccessToken = new CasTGTOAuth2AccessToken(casTGT, tokenExpirationConfig.getAccessTokenValiditySeconds());
                    break;
                }
            }
        }

        return returnAccessToken != null? returnAccessToken : accessToken;
    }
}
