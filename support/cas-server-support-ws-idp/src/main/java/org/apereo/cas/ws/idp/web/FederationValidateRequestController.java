package org.apereo.cas.ws.idp.web;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apereo.cas.BaseFederationRequestController;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.ws.idp.FederationConstants;
import org.apereo.cas.ws.idp.api.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.api.RealmAwareIdentityProvider;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Date;

/**
 * This is {@link FederationValidateRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class FederationValidateRequestController extends BaseFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FederationValidateRequestController.class);

    public FederationValidateRequestController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                               final ServicesManager servicesManager,
                                               final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                               final CasConfigurationProperties casProperties) {
        super(identityProviderConfigurationService, servicesManager,
                webApplicationServiceFactory, casProperties);
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = FederationConstants.ENDPOINT_FEDERATION_REQUEST)
    protected void handleFederationRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        final FederationRequest fedRequest = FederationRequest.of(request);
        final RealmAwareIdentityProvider idp = this.identityProviderConfigurationService.getIdentityProvider(
                casProperties.getAuthn().getWsfedIdP().getIdp().getRealm());

        switch (fedRequest.getWa().toLowerCase()) {
            case FederationConstants.WSIGNOUT10:
            case FederationConstants.WSIGNOUT_CLEANUP10:
                selectSignOutProcess(fedRequest, idp, response, request);
                break;
            case FederationConstants.WSIGNIN10:
            default:
                selectWsFedProcess(fedRequest, idp, response, request);
                break;
        }
    }

    private void selectWsFedProcess(final FederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                                    final HttpServletResponse response, final HttpServletRequest request) {
        if (StringUtils.isNotBlank(fedRequest.getWresult())) {
            signinResponse(fedRequest, idp, response, request);
        } else if (StringUtils.isNotBlank(fedRequest.getWtrealm())) {
            signinRequest(fedRequest, idp, response, request);
        }
    }

    private void signinRequest(final FederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                               final HttpServletResponse response, final HttpServletRequest request) {
        if (idp.getAuthenticationURIs().containsKey(fedRequest.getWauth())) {
            if (shouldRedirect(fedRequest, response, request)) {
                redirectToIdentityProvider(fedRequest, response, request);
            } else {
                validateWReply(fedRequest, response, request);
            }
        }
    }

    private void validateWReply(final FederationRequest fedRequest, final HttpServletResponse response,
                                final HttpServletRequest request) {

    }

    private String constructServiceUrl(final HttpServletRequest request,
                                       final HttpServletResponse response,
                                       final FederationRequest federationRequest) {
        try {
            final URIBuilder builder = new URIBuilder(this.callbackService.getId());
            builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wa", federationRequest.getWa()));
            builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wreply", federationRequest.getWreply()));
            builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wtrealm", federationRequest.getWtrealm()));

            if (StringUtils.isNotBlank(federationRequest.getWctx())) {
                builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wctx", federationRequest.getWctx()));
            }
            if (StringUtils.isNotBlank(federationRequest.getWfresh())) {
                builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wfresh", federationRequest.getWfresh()));
            }
            if (StringUtils.isNotBlank(federationRequest.getWhr())) {
                builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("whr", federationRequest.getWhr()));
            }
            if (StringUtils.isNotBlank(federationRequest.getWreq())) {
                builder.getQueryParams().add(new URIBuilder.BasicNameValuePair("wreq", federationRequest.getWreq()));
            }

            final URI url = builder.build();

            LOGGER.debug("Built service callback url [{}]", url);
            return org.jasig.cas.client.util.CommonUtils.constructServiceUrl(request, response,
                    url.toString(), casProperties.getServer().getName(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    private void redirectToIdentityProvider(final FederationRequest fedRequest, final HttpServletResponse response,
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

    private boolean shouldRedirect(final FederationRequest federationRequest,
                                   final HttpServletResponse response,
                                   final HttpServletRequest request) {
        return isTokenExpired(federationRequest, response, request) || isAuthenticationRequired(federationRequest, response, request);
    }

    private boolean isAuthenticationRequired(final FederationRequest federationRequest,
                                             final HttpServletResponse response,
                                             final HttpServletRequest request) {
        if (StringUtils.isNotBlank(federationRequest.getWfresh()) || NumberUtils.isCreatable(federationRequest.getWfresh())) {
            return false;
        }

        final long ttl = Long.parseLong(federationRequest.getWfresh().trim());
        if (ttl == 0) {
            return true;
        }

        final long ttlMs = ttl * 60L * 1000L;
        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

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

    private boolean isTokenExpired(final FederationRequest federationRequest,
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

    private void signinResponse(final FederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                                final HttpServletResponse response,
                                final HttpServletRequest request) {

    }

    private void selectSignOutProcess(final FederationRequest fedRequest, final RealmAwareIdentityProvider idp,
                                      final HttpServletResponse response,
                                      final HttpServletRequest request) {

    }
}
