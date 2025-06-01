package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OidcDefaultTokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OidcDefaultTokenGenerator extends OAuth20DefaultTokenGenerator {
    public OidcDefaultTokenGenerator(final TicketFactory ticketFactory,
                                     final TicketRegistry ticketRegistry,
                                     final PrincipalResolver principalResolver,
                                     final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
                                     final CasConfigurationProperties casProperties) {
        super(ticketFactory, ticketRegistry, principalResolver, profileScopeToAttributesFilter, casProperties);
    }

    @Override
    protected Authentication finalizeAuthentication(final AccessTokenRequestContext tokenRequestContext,
                                                    final AuthenticationBuilder authenticationBuilder) {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.CIBA) {
            FunctionUtils.doIfNotNull(tokenRequestContext.getCibaRequestId(),
                __ -> authenticationBuilder.addAttribute(OidcConstants.AUTH_REQ_ID, tokenRequestContext.getCibaRequestId()));
        }

        return super.finalizeAuthentication(tokenRequestContext, authenticationBuilder);
    }
}
