package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryDelegationProperties;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This is {@link AzureActiveDirectoryAuthenticationAction} that extracts basic authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AzureActiveDirectoryAuthenticationAction extends AbstractAction {

    private static final String PROVIDERURL = "MicrosoftLoginProviderUrl";

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryAuthenticationAction.class);

    private CasConfigurationProperties casProperties;

    public AzureActiveDirectoryAuthenticationAction(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            final BasicAuthExtractor extractor = new BasicAuthExtractor(this.getClass().getSimpleName());
            final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
            final UsernamePasswordCredentials credentials = extractor.extract(webContext);
            if (credentials == null) {
                return routeToInstanceForAuthentication(requestContext);
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    private Event routeToInstanceForAuthentication(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();

        final Service service = (Service) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        if (service != null) {
            session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        }

        final String url = getAuthorizationUrl(request);
        LOGGER.info("Preparing to redirect to the IdP [{}]", url);
        context.getFlowScope().put(PROVIDERURL, url);
        return error();
    }

    private String getAuthorizationUrl(final HttpServletRequest request) {
        final AzureActiveDirectoryDelegationProperties azure = casProperties.getAuthn().getAzureAd();
        final String url = azure.getInstance() + azure.getTenant()
            + "oauth2/authorize?response_type=id_token&client_id=%s&redirect_uri=%s&"
            + azure.getExtraQueryParameters();

        return String.format(url, azure.getClientId(), request.getRequestURI());
    }
}
