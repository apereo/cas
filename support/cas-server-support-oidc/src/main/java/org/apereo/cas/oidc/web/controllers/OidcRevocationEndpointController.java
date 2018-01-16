package org.apereo.cas.oidc.web.controllers;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcRevocationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcRevocationEndpointController extends BaseOAuth20Controller {


    public OidcRevocationEndpointController(final ServicesManager servicesManager,
                                            final TicketRegistry ticketRegistry,
                                            final OAuth20Validator validator,
                                            final AccessTokenFactory accessTokenFactory,
                                            final PrincipalFactory principalFactory,
                                            final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                            final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                            final CasConfigurationProperties casProperties,
                                            final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter,
                casProperties, ticketGrantingTicketCookieGenerator);
    }

    /**
     * Handle request for revocation.
     *
     * @param request  the request
     * @param response the response
     * @return the jwk set
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                        final HttpServletResponse response) {
        try {
            final CredentialsExtractor<UsernamePasswordCredentials> authExtractor = new BasicAuthExtractor(getClass().getSimpleName());
            final UsernamePasswordCredentials credentials = authExtractor.extract(Pac4jUtils.getPac4jJ2EContext(request, response));
            if (credentials == null) {
                throw new IllegalArgumentException("No credentials are provided to verify introspection on the access token");
            }

            final OAuthRegisteredService service = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, credentials.getUsername());
            if (this.validator.checkServiceValid(service)
                    && this.validator.checkParameterExist(request, OAuth20Constants.ACCESS_TOKEN)
                    && this.validator.checkClientSecret(service, credentials.getPassword())) {
                final String token = request.getParameter(OidcConstants.TOKEN);
                if (StringUtils.isNotBlank(token)) {
                    this.ticketRegistry.deleteTicket(token);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
