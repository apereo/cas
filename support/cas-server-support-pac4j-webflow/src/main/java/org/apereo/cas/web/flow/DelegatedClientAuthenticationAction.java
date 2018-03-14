package org.apereo.cas.web.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an action to put at the beginning of the webflow.
 * <p>
 * Before any authentication, redirection urls are computed for the different clients defined as well as the theme,
 * locale, method and service are saved into the web session.</p>
 * After authentication, appropriate information are expected on this callback url to finish the authentication
 * process with the provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DelegatedClientAuthenticationAction extends AbstractAction {
    /**
     * All the urls and names of the pac4j clients.
     */
    public static final String PAC4J_URLS = "pac4jUrls";

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");
    private static final Pattern PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN = Pattern.compile("\\W");

    private final Clients clients;

    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final CentralAuthenticationService centralAuthenticationService;

    private final ServicesManager servicesManager;
    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;
    private final DelegatedClientWebflowManager delegatedClientWebflowManager;
    private final DelegatedSessionCookieManager delegatedSessionCookieManager;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        final String clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        LOGGER.debug("Delegated authentication is handled by client name [{}]", clientName);
        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            return stopWebflow();
        }

        final J2EContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
        if (StringUtils.isNotBlank(clientName)) {
            final Service service = restoreAuthenticationRequestInContext(context, webContext, clientName);
            final BaseClient<Credentials, CommonProfile> client = findDelegatedClientByName(request, clientName, service);

            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
                if (credentials == null) {
                    throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
                }
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
                return stopWebflow();
            }

            if (credentials != null) {
                return establishDelegatedAuthenticationSession(context, service, credentials, client);
            }
        }

        prepareForLoginPage(context);

        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            return stopWebflow();
        }
        return error();
    }

    private Event establishDelegatedAuthenticationSession(final RequestContext context, final Service service,
                                                          final Credentials credentials, final BaseClient client) {
        final ClientCredential clientCredential = new ClientCredential(credentials, client.getName());
        final AuthenticationResult authenticationResult =
            this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, clientCredential);
        final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        return success();
    }

    private BaseClient<Credentials, CommonProfile> findDelegatedClientByName(final HttpServletRequest request, final String clientName, final Service service) {
        final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
        LOGGER.debug("Delegated authentication client is [{}] with service [{}}", client, service);
        if (service != null) {
            request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            if (!isDelegatedClientAuthorizedForService(client, service)) {
                LOGGER.warn("Delegated client [{}] is not authorized by service [{}]", client, service);
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
            }
        }
        return client;
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     */
    protected void prepareForLoginPage(final RequestContext context) {
        final Service service = WebUtils.getService(context);
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

        final Set<ProviderLoginPageConfiguration> urls = new LinkedHashSet<>();
        this.clients.findAllClients()
            .stream()
            .filter(client -> client instanceof IndirectClient && isDelegatedClientAuthorizedForService(client, service))
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                try {
                    final Optional<ProviderLoginPageConfiguration> provider = buildProviderConfiguration(client, webContext);
                    provider.ifPresent(urls::add);
                } catch (final Exception e) {
                    LOGGER.error("Cannot process client [{}]", client, e);
                }
            });
        if (!urls.isEmpty()) {
            context.getFlowScope().put(PAC4J_URLS, urls);
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no clients are configured, or the current access strategy rules prohibit CAS from using authentication providers for this request.");
        }
    }

    private Optional<ProviderLoginPageConfiguration> buildProviderConfiguration(final IndirectClient client, final WebContext webContext) {
        final String name = client.getName();
        final Matcher matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
        final String type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();
        final String redirectUrl = DelegatedClientNavigationController.ENDPOINT_REDIRECT
            + "?" + Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER + "=" + name;
        final boolean autoRedirect = (Boolean) client.getCustomProperties().getOrDefault("autoRedirect", Boolean.FALSE);
        final ProviderLoginPageConfiguration p = new ProviderLoginPageConfiguration(name, redirectUrl, type, getCssClass(name), autoRedirect);
        return Optional.of(p);
    }


    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param name Name of the provider
     */
    private String getCssClass(final String name) {
        String computedCssClass = "fa fa-lock";
        if (name != null) {
            computedCssClass = computedCssClass.concat(" " + PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN.matcher(name).replaceAll("-"));
        }
        LOGGER.debug("cssClass for [{}] is [{}]", name, computedCssClass);
        return computedCssClass;
    }

    private Event stopWebflow() {
        return new Event(this, CasWebflowConstants.TRANSITION_ID_STOP);
    }

    /**
     * Determine if request has errors.
     *
     * @param request the request
     * @param status  the status
     * @return the optional model and view, if request is an error.
     */
    public static Optional<ModelAndView> hasDelegationRequestFailed(final HttpServletRequest request, final int status) {
        final Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey("error") || params.containsKey("error_code") || params.containsKey("error_description") || params.containsKey("error_message")) {
            final Map<String, Object> model = new HashMap<>();
            if (params.containsKey("error_code")) {
                model.put("code", StringEscapeUtils.escapeHtml4(request.getParameter("error_code")));
            } else {
                model.put("code", status);
            }
            model.put("error", StringEscapeUtils.escapeHtml4(request.getParameter("error")));
            model.put("reason", StringEscapeUtils.escapeHtml4(request.getParameter("error_reason")));
            if (params.containsKey("error_description")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_description")));
            } else if (params.containsKey("error_message")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_message")));
            }
            model.put(CasProtocolConstants.PARAMETER_SERVICE, request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
            model.put("client", StringEscapeUtils.escapeHtml4(request.getParameter("client_name")));
            LOGGER.debug("Delegation request has failed. Details are [{}]", model);
            return Optional.of(new ModelAndView("casPac4jStopWebflow", model));
        }
        return Optional.empty();
    }

    private boolean isDelegatedClientAuthorizedForService(final Client client, final Service service) {
        if (service == null || StringUtils.isBlank(service.getId())) {
            LOGGER.debug("Can not evaluate delegated authentication policy since no service was provided in the request while processing client [{}]", client);
            return true;
        }

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("Service access for [{}] is denied", registeredService);
            return false;
        }
        LOGGER.debug("Located registered service definition [{}] matching [{}]", registeredService, service);
        final AuditableContext context = AuditableContext.builder()
            .registeredService(registeredService)
            .properties(CollectionUtils.wrap(Client.class.getSimpleName(), client.getName()))
            .build();
        final AuditableExecutionResult result = delegatedAuthenticationPolicyEnforcer.execute(context);
        if (!result.isExecutionFailure()) {
            LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, client);
            return true;
        }
        LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), client);
        return false;
    }

    private Service restoreAuthenticationRequestInContext(final RequestContext requestContext, final J2EContext webContext, final String clientName) {
        delegatedSessionCookieManager.restore(webContext);
        final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
        final Service service = delegatedClientWebflowManager.retrieve(requestContext, webContext, client);
        return service;
    }

    /**
     * The Provider login page configuration.
     */
    @AllArgsConstructor
    @Getter
    @ToString
    public static class ProviderLoginPageConfiguration implements Serializable {

        private static final long serialVersionUID = 6216882278086699364L;

        private final String name;
        private final String redirectUrl;
        private final String type;
        private final String cssClass;
        private final boolean autoRedirect;
    }
}
