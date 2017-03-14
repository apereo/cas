package org.apereo.cas.ws.idp.web;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.RealmAwareIdentityProvider;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * This is {@link WSWSFederationValidateRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WSWSFederationValidateRequestController extends BaseWSFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSWSFederationValidateRequestController.class);

    public WSWSFederationValidateRequestController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final CasConfigurationProperties casProperties,
                                                   final AuthenticationServiceSelectionStrategy serviceSelectionStrategy,
                                                   final HttpClient httpClient,
                                                   final SecurityTokenTicketFactory securityTokenTicketFactory,
                                                   final TicketRegistry ticketRegistry) {
        super(identityProviderConfigurationService, servicesManager,
                webApplicationServiceFactory, casProperties, serviceSelectionStrategy, httpClient,
                securityTokenTicketFactory, ticketRegistry);
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_REQUEST)
    protected void handleFederationRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        final WSFederationRequest fedRequest = WSFederationRequest.of(request);
        final RealmAwareIdentityProvider idp = this.identityProviderConfigurationService.getIdentityProvider(
                casProperties.getAuthn().getWsfedIdP().getIdp().getRealm());

        switch (fedRequest.getWa().toLowerCase()) {
            case WSFederationConstants.WSIGNOUT10:
            case WSFederationConstants.WSIGNOUT_CLEANUP10:
                LOGGER.warn("Federation request [{}] is not yet supported", fedRequest.getWa());
                break;
            case WSFederationConstants.WSIGNIN10:
            default:
                selectWsFedProcess(fedRequest, idp, response, request);
                break;
        }
    }

    private void selectWsFedProcess(final WSFederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                                    final HttpServletResponse response, final HttpServletRequest request) {
        if (StringUtils.isNotBlank(fedRequest.getWtrealm())) {
            signinRequest(fedRequest, idp, response, request);
        }
    }

    private void signinRequest(final WSFederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                               final HttpServletResponse response, final HttpServletRequest request) {
        if (idp.getAuthenticationURIs().containsKey(fedRequest.getWauth())) {
            if (shouldRedirect(fedRequest, response, request)) {
                redirectToIdentityProvider(fedRequest, response, request);
            }
        }
    }


    private void redirectToIdentityProvider(final WSFederationRequest fedRequest, final HttpServletResponse response,
                                            final HttpServletRequest request) {
        try {
            final String serviceUrl = constructServiceUrl(request, response, fedRequest);
            LOGGER.debug("Created service url [{}]", serviceUrl);

            final String initialUrl = CommonUtils.constructRedirectUrl(casProperties.getServer().getLoginUrl(),
                    CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, false, false);
            LOGGER.debug("Redirecting authN request to \"[{}]\"", initialUrl);
            final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
            authenticationRedirectStrategy.redirect(request, response, initialUrl);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private boolean shouldRedirect(final WSFederationRequest fedRequest,
                                   final HttpServletResponse response,
                                   final HttpServletRequest request) {
        return isTokenExpired(fedRequest, response, request) || isAuthenticationRequired(fedRequest, response, request);
    }

    private boolean isAuthenticationRequired(final WSFederationRequest fedRequest,
                                             final HttpServletResponse response,
                                             final HttpServletRequest request) {
        if (StringUtils.isBlank(fedRequest.getWfresh()) || NumberUtils.isCreatable(fedRequest.getWfresh())) {
            return false;
        }

        final long ttl = Long.parseLong(fedRequest.getWfresh().trim());
        if (ttl == 0) {
            return true;
        }
        
        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        final long ttlMs = ttl * 60L * 1000L;
        if (ttlMs > 0) {
            final Date createdDate = idpToken.getCreated();
            if (createdDate != null) {
                final Date expiryDate = new Date();
                expiryDate.setTime(createdDate.getTime() + ttlMs);
                if (expiryDate.before(new Date())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTokenExpired(final WSFederationRequest fedRequest,
                                   final HttpServletResponse response,
                                   final HttpServletRequest request) {
        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        return idpToken.isExpired();
    }

    private SecurityToken getSecurityTokenFromRequest(final HttpServletRequest request) {
        
        return (SecurityToken) request.getAttribute("idpSecurityToken");
    }
}
