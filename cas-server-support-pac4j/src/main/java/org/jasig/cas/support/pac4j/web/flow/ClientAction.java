package org.jasig.cas.support.pac4j.web.flow;

import java.util.LinkedHashMap;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.DefaultAuthenticationContextBuilder;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.principal.ClientCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.ClientType;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

/**
 * This class represents an action to put at the beginning of the webflow.
 * <p>
 * Before any authentication, redirection urls are computed for the different clients defined as well as the theme,
 * locale, method and service are saved into the web session.</p>
 * After authentication, appropriate information are expected on this callback url to finish the authentication
 * process with the provider.
 * @author Jerome Leleu
 * @since 3.5.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@Component("clientAction")
public final class ClientAction extends AbstractAction {
    /**
     * All the urls and names of the pac4j clients.
     */
    public static final String PAC4J_URLS = "pac4jUrls";

    /**
     * Supported protocols.
     */
    private static final Set<ClientType> SUPPORTED_PROTOCOLS = ImmutableSet.of(ClientType.CAS_PROTOCOL, ClientType.OAUTH_PROTOCOL,
            ClientType.OPENID_PROTOCOL, ClientType.SAML_PROTOCOL, ClientType.OPENID_CONNECT_PROTOCOL);

    /**
     * The logger.
     */
    private final transient Logger logger = LoggerFactory.getLogger(ClientAction.class);

    /**
     * The clients used for authentication.
     */
    @NotNull
    @Autowired
    @Qualifier("builtClients")
    private Clients clients;

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    /**
     * The service for CAS authentication.
     */
    @NotNull
    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    static {
        ProfileHelper.setKeepRawData(true);
    }

    /**
     * Build the ClientAction.
     */
    public ClientAction() {}

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // get client
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        logger.debug("clientName: {}", clientName);

        // it's an authentication
        if (StringUtils.isNotBlank(clientName)) {
            // get client
            final BaseClient<Credentials, CommonProfile> client =
                    (BaseClient<Credentials, CommonProfile>) this.clients
                    .findClient(clientName);
            logger.debug("client: {}", client);

            // Only supported protocols
            final ClientType clientType = client.getClientType();
            if (!SUPPORTED_PROTOCOLS.contains(clientType)) {
                throw new TechnicalException("Only CAS, OAuth, OpenID and SAML protocols are supported: " + client);
            }

            // get credentials
            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                logger.debug("credentials: {}", credentials);
            } catch (final RequiresHttpAction e) {
                logger.debug("requires http action: {}", e);
                response.flushBuffer();
                final ExternalContext externalContext = ExternalContextHolder.getExternalContext();
                externalContext.recordResponseComplete();
                return new Event(this, "stop");
            }

            // retrieve parameters from web session
            final Service service = (Service) session.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            logger.debug("retrieve service: {}", service);
            if (service != null) {
                request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            restoreRequestAttribute(request, session, ThemeChangeInterceptor.DEFAULT_PARAM_NAME);
            restoreRequestAttribute(request, session, LocaleChangeInterceptor.DEFAULT_PARAM_NAME);
            restoreRequestAttribute(request, session, CasProtocolConstants.PARAMETER_METHOD);

            // credentials not null -> try to authenticate
            if (credentials != null) {
                final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                        this.authenticationSystemSupport.getPrincipalElectionStrategy());
                final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(new ClientCredential(credentials));
                this.authenticationSystemSupport.getAuthenticationTransactionManager().handle(transaction,  builder);
                final AuthenticationContext authenticationContext = builder.build(service);
                final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationContext);
                WebUtils.putTicketGrantingTicketInScopes(context, tgt);
                return success();
            }

        }

        // no or aborted authentication : go to login page
        prepareForLoginPage(context);
        return error();
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     */
    protected void prepareForLoginPage(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // save parameters in web session
        final WebApplicationService service = WebUtils.getService(context);
        logger.debug("save service: {}", service);
        session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        saveRequestParameter(request, session, ThemeChangeInterceptor.DEFAULT_PARAM_NAME);
        saveRequestParameter(request, session, LocaleChangeInterceptor.DEFAULT_PARAM_NAME);
        saveRequestParameter(request, session, CasProtocolConstants.PARAMETER_METHOD);

        final LinkedHashMap<String, String> urls = new LinkedHashMap<>();
        // for all clients, generate redirection urls
        for (final Client client : this.clients.findAllClients()) {
            final IndirectClient indirectClient = (IndirectClient) client;
            // clean Client suffix for default names
            final String name = client.getName().replace("Client", "");
            final String redirectionUrl = indirectClient.getRedirectionUrl(webContext);
            logger.debug("{} -> {}", name, redirectionUrl);
            urls.put(name, redirectionUrl);
        }
        context.getFlowScope().put(PAC4J_URLS, urls);
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name The name of the parameter
     */
    private void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session,
            final String name) {
        final String value = (String) session.getAttribute(name);
        request.setAttribute(name, value);
    }

    /**
     * Save a request parameter in the web session.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name The name of the parameter
     */
    private void saveRequestParameter(final HttpServletRequest request, final HttpSession session,
            final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }

    public Clients getClients() {
        return clients;
    }

    public void setClients(final Clients clients) {
        this.clients = clients;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return authenticationSystemSupport;
    }
}
