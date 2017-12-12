package org.apereo.cas.web.flow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AzureActiveDirectoryCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryDelegationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link AzureActiveDirectoryAuthenticationAction} that extracts basic authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AzureActiveDirectoryAuthenticationAction extends AbstractAction {

    private static final String PROVIDERURL = "MicrosoftLoginProviderUrl";

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryAuthenticationAction.class);

    private final CasConfigurationProperties casProperties;

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;

    public AzureActiveDirectoryAuthenticationAction(final CasConfigurationProperties casProperties,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final ServicesManager servicesManager) {
        this.casProperties = casProperties;
        this.centralAuthenticationService = centralAuthenticationService;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.servicesManager = servicesManager;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            final String code = request.getParameter("code");
            if (StringUtils.isNotBlank(code)) {
                final Map<String, String> accessTokenMap = obtainAccessToken(request);
                if (accessTokenMap == null || accessTokenMap.isEmpty()) {
                    LOGGER.warn("Could not obtain an ID token for code [{}]", code);
                    return null;
                }

                finalizeAuthentication(accessTokenMap, requestContext);
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
            }
            LOGGER.debug("No code is provided via the request. Routing to instance for authorization...");
            return routeToInstanceForAuthentication(requestContext);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    private void finalizeAuthentication(final Map<String, String> map, final RequestContext context) {
        LOGGER.debug("Creating azure active directory credential based on [{}]", map);
        final AzureActiveDirectoryCredential credential = AzureActiveDirectoryCredential.from(map);

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();
        final Service service = (Service) session.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);

        LOGGER.debug("Creating final authentication result based on the given credential");
        final AuthenticationResult authenticationResult =
            this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

        LOGGER.debug("Attempting to create a ticket-granting ticket for the authentication result");
        WebUtils.putService(context, service);
        WebUtils.putTicketGrantingTicketInScopes(context,
            this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult));
        LOGGER.debug("Token validated and new [{}] created: [{}]", credential.getClass().getName(), credential);
    }

    private Event routeToInstanceForAuthentication(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();

        final Service service = (Service) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        if (service != null) {
            LOGGER.debug("Stored service parameter [{}] in session", service.getId());
            session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        }

        final String url = getAuthorizationUrl(request);
        LOGGER.debug("Preparing to redirect to the IdP [{}]", url);
        context.getFlowScope().put(PROVIDERURL, url);
        return error();
    }

    private Map<String, String> obtainAccessToken(final HttpServletRequest request) {
        try {
            final AzureActiveDirectoryDelegationProperties azure = casProperties.getAuthn().getAzureAd();
            final String url = azure.getInstance() + azure.getTenant() + "oauth2/token";
            final String code = request.getParameter("code");

            LOGGER.debug("Validating OAuth code [{}] for client id [{}] and redirect URI [{}]", code, azure.getClientId(), getRedirectUri());
            final List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("client_id", azure.getClientId()));
            params.add(new BasicNameValuePair("client_secret", azure.getClientSecret()));
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("redirect_uri", getRedirectUri()));
            params.add(new BasicNameValuePair("resource", azure.getClientId()));
            final HttpResponse response = HttpUtils.executePost(url, new UrlEncodedFormEntity(params));

            try (StringWriter writer = new StringWriter()) {
                IOUtils.copy(response.getEntity().getContent(), writer, StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOGGER.error("OAuth code token response is [{}]", writer.toString());
                    return null;
                }

                LOGGER.debug("OAuth code token response is [{}]", writer.toString());
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(writer.toString(),
                    new TypeReference<Map<String, String>>() {
                    });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private String getAuthorizationUrl(final HttpServletRequest request) {
        final String nonce = UUID.randomUUID().toString();
        final AzureActiveDirectoryDelegationProperties azure = casProperties.getAuthn().getAzureAd();
        final String url = azure.getInstance() + azure.getTenant()
            + "oauth2/authorize?response_type=code&nonce=%s&client_id=%s&redirect_uri=%s&"
            + azure.getExtraQueryParameters();

        return String.format(url, nonce, azure.getClientId(), getRedirectUri());
    }

    private String getRedirectUri() {
        return casProperties.getServer().getLoginUrl();
    }
}
