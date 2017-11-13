package org.apereo.cas.support.pac4j.web.flow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;


/**
 * An action that gives preference to SAML2 Single Logout over a forward to the specified service.
 * 
 * Normally, if the "service" parameter is specified for the logout call, the web flow redirects to the service at the end. However, this
 * means that no SAML2 Single Logout will occur because the view actually used is different. To overcome this issue, this action examines
 * the client; if it is a SAML2 client and it has at least one SLO service defined, the "service" parameter is completely removed from the
 * flow scope, no redirect to the service occurs (although the caller wished so) and the SLO can be performed.
 * 
 * To be inserted (ideally) to the "on-entry" section of state "finishLogout", either declaratively or programmatically.
 * 
 * @author jkacer
 * 
 * @since 5.2.0
 */
public class IgnoreServiceRedirectUrlForSamlAction extends AbstractAction {

    
    /**
     * Name of the web flow attribute that holds the URL where to redirect after the flow.
     * 
     * @see WebUtils#putLogoutRedirectUrl(RequestContext, String)
     * @see LogoutAction#doExecuteInternal()
     */
    public static final String FLOW_ATTR_LOGOUT_REDIR_URL = "logoutRedirectUrl";

    private final Logger logger2 = LoggerFactory.getLogger(IgnoreServiceRedirectUrlForSamlAction.class);

    private final Clients clients;


    /**
     * Creates a new Ignore Service Redirect action.
     * 
     * @param clients
     *            All PAC4J clients.
     */
    public IgnoreServiceRedirectUrlForSamlAction(final Clients clients) {
        super();
        this.clients = clients;
    }


    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        final J2EContext context = Pac4jUtils.getPac4jJ2EContext(request, response);

        final String currentClientName = Pac4jUtils.findCurrentClientName(context);
        final Client<?, ?> client = (currentClientName == null) ? null : clients.findClient(currentClientName);

        if (shouldServiceRedirectBeIgnored(client, context)) {
            requestContext.getFlowScope().remove(FLOW_ATTR_LOGOUT_REDIR_URL);
            logger2.debug("The Logout Redirection URL has been removed from the web low scope in order to allow for SAML2 SLO.");
        }

        return null;
    }


    /**
     * Decides if the "service" parameter should be ignored.
     * 
     * It should be ignored for SAML2 clients that have at least one SLO service.
     * 
     * @param client
     *            The currently used client.
     * @param wc
     *            The current web context (request + response).
     * 
     * @return True if the parameter should be ignored, false otherwise.
     */
    private boolean shouldServiceRedirectBeIgnored(final Client<?, ?> client, final WebContext wc) {
        if (!(client instanceof SAML2Client)) {
            return false;
        }

        final SAML2Client saml2Client = (SAML2Client) client;
        final SAML2MessageContext samlContext = saml2Client.getContextProvider().buildContext(wc);
        final IDPSSODescriptor idpSsoDescriptor = (samlContext == null) ? null : samlContext.getIDPSSODescriptor();
        final List<SingleLogoutService> sloServices = (idpSsoDescriptor == null) ? null : idpSsoDescriptor.getSingleLogoutServices();

        return (sloServices != null) && (!sloServices.isEmpty());
    }

}
