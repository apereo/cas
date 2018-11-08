package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {
    public AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(final ServicesManager servicesManager,
                                                                                 final TicketRegistry ticketRegistry,
                                                                                 final CentralAuthenticationService centralAuthenticationService,
                                                                                 final OAuthProperties oAuthProperties,
                                                                                 final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties, webApplicationServiceServiceFactory);
    }

    @Override
    protected AccessTokenRequestDataHolder extractInternal(final HttpServletRequest request, final HttpServletResponse response,
                                                           final AccessTokenRequestDataHolder.AccessTokenRequestDataHolderBuilder builder) {
        val challenge = request.getParameter(OAuth20Constants.CODE_CHALLENGE);
        return builder.codeVerifier(challenge).build();
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        val challenge = context.getParameter(OAuth20Constants.CODE_VERIFIER);
        return StringUtils.isNotBlank(challenge) && super.supports(context);
    }

    @Override
    public boolean requestMustBeAuthenticated() {
        return true;
    }
}
