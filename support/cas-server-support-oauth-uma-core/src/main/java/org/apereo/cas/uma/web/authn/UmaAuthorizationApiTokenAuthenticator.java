package org.apereo.cas.uma.web.authn;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

/**
 * This is {@link UmaAuthorizationApiTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaAuthorizationApiTokenAuthenticator extends BaseUmaTokenAuthenticator {

    public UmaAuthorizationApiTokenAuthenticator(final TicketRegistry ticketRegistry,
                                                 final JwtBuilder accessTokenJwtBuilder,
                                                 final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
                                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        super(ticketRegistry, accessTokenJwtBuilder, profileScopeToAttributesFilter, authenticationAttributeReleasePolicy);
    }

    @Override
    protected String getRequiredScope() {
        return OAuth20Constants.UMA_AUTHORIZATION_SCOPE;
    }
}
