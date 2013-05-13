package org.jasig.cas.support.oauth.token;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

/**
 * Represents an access token that contains a CAS TGT ID as the value and the TGT expiration information in access
 * token form.
 *
 * @author Joe McCall
 */
public class CasTGTOAuth2AccessToken extends DefaultOAuth2AccessToken {

    private static final long serialVersionUID = 1L;

    public CasTGTOAuth2AccessToken(final TicketGrantingTicket casTGT, final Long timeToKillInSeconds) {
        super(casTGT.getId());

        long timeLeft =
                TimeUnit.SECONDS.toMillis(timeToKillInSeconds) -
                System.currentTimeMillis() + casTGT.getCreationTime();

        this.setExpiration(new Date(System.currentTimeMillis() + timeLeft));
        this.setExpiresIn((int) timeLeft);

        // No scope... for now
    }

}
