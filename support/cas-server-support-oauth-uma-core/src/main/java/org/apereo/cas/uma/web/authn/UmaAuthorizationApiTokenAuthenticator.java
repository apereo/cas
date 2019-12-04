package org.apereo.cas.uma.web.authn;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

/**
 * This is {@link UmaAuthorizationApiTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaAuthorizationApiTokenAuthenticator extends BaseUmaTokenAuthenticator {

    public UmaAuthorizationApiTokenAuthenticator(final TicketRegistry ticketRegistry,
                                                 final JwtBuilder accessTokenJwtBuilder) {
        super(ticketRegistry, accessTokenJwtBuilder);
    }

    @Override
    protected String getRequiredScope() {
        return OAuth20Constants.UMA_AUTHORIZATION_SCOPE;
    }
}
