package org.jasig.cas.support.oauth.token;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

public class CasTGTOAuth2AccessToken extends DefaultOAuth2AccessToken {

    private static final long serialVersionUID = 1L;

    public CasTGTOAuth2AccessToken(TicketGrantingTicket casTGT, Long timeToKillInSeconds) {
        super(casTGT.getId());
        
        long timeLeft = 
                TimeUnit.SECONDS.toMillis(timeToKillInSeconds) - 
                System.currentTimeMillis() + casTGT.getCreationTime();
        
        this.setExpiration(new Date(System.currentTimeMillis() + timeLeft));
        this.setExpiresIn((int) timeLeft);
        
        // No scope... for now
    }

}
