package org.apereo.cas.support.wsfederation.web.flow;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.saml1.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class represents an action in the webflow to retrieve WsFederation information on the callback url which is
 * the webflow url (/login).
 *
 * @author John Gasper
 * @since 4.2.0
 */
@RefreshScope
@Component("wsFederationAction")
public class WsFederationAction extends AbstractAction {

    private static final String LOCALE = "locale";
    private static final String METHOD = "method";
    private static final String PROVIDERURL = "WsFederationIdentityProviderUrl";
    private static final String QUERYSTRING = "?wa=wsignin1.0&wtrealm=";
    private static final String SERVICE = "service";
    private static final String THEME = "theme";
    private static final String WA = "wa";
    private static final String WRESULT = "wresult";
    private static final String WSIGNIN = "wsignin1.0";

    private transient Logger logger = LoggerFactory.getLogger(WsFederationAction.class);

    
    @Autowired
    @Qualifier("wsFederationHelper")
    private WsFederationHelper wsFederationHelper;

    
    @Autowired
    @Qualifier("wsFedConfig")
    private WsFederationConfiguration configuration;

    
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    /**
     * Executes the webflow action.
     *
     * @param context the context
     * @return the event
     * @throws Exception all unhandled exceptions
     */
    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            final HttpSession session = request.getSession();

            final String wa = request.getParameter(WA);

            // it's an authentication
            if (StringUtils.isNotBlank(wa) && wa.equalsIgnoreCase(WSIGNIN)) {
                final String wresult = request.getParameter(WRESULT);
                logger.debug("Parameter [{}] received: {}", WRESULT, wresult);

                if (StringUtils.isBlank(wresult)) {
                    logger.error("No {} parameter is found", WRESULT);
                    return error();
                }

                // create credentials
                final Assertion assertion = this.wsFederationHelper.parseTokenFromString(wresult);

                if (assertion == null) {
                    logger.error("Could not validate assertion via parsing the token from {}", WRESULT);
                    return error();
                }

                if (!this.wsFederationHelper.validateSignature(assertion, this.configuration)) {
                    logger.error("WS Requested Security Token is blank or the signature is not valid.");
                    return error();
                }

                try {

                    final WsFederationCredential credential = this.wsFederationHelper.createCredentialFromToken(assertion);
                    if (credential != null && credential.isValid(this.configuration.getRelyingPartyIdentifier(),
                            this.configuration.getIdentityProviderIdentifier(),
                            this.configuration.getTolerance())) {

                        if (this.configuration.getAttributeMutator() != null) {
                            this.configuration.getAttributeMutator().modifyAttributes(credential.getAttributes());
                        }
                    } else {
                        logger.warn("SAML assertions are blank or no longer valid.");
                        return error();
                    }

                    final Service service = (Service) session.getAttribute(SERVICE);
                    context.getFlowScope().put(SERVICE, service);
                    restoreRequestAttribute(request, session, THEME);
                    restoreRequestAttribute(request, session, LOCALE);
                    restoreRequestAttribute(request, session, METHOD);

                    final AuthenticationResult authenticationResult =
                            this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

                    WebUtils.putTicketGrantingTicketInScopes(context,
                            this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult));

                    logger.info("Token validated and new {} created: {}", credential.getClass().getName(), credential);
                    return success();

                } catch (final AbstractTicketException e) {
                    logger.error(e.getMessage(), e);
                    return error();
                }



            } else { // no authentication : go to login page


                // save parameters in web session
                final Service service = (Service) context.getFlowScope().get(SERVICE);
                if (service != null) {
                    session.setAttribute(SERVICE, service);
                }
                saveRequestParameter(request, session, THEME);
                saveRequestParameter(request, session, LOCALE);
                saveRequestParameter(request, session, METHOD);

                final String key = PROVIDERURL;
                final String authorizationUrl = this.configuration.getIdentityProviderUrl()
                        + QUERYSTRING
                        + this.configuration.getRelyingPartyIdentifier();

                logger.info("Preparing to redirect to the IdP {}", authorizationUrl);
                context.getFlowScope().put(key, authorizationUrl);
            }

            logger.debug("Redirecting to the IdP");
            return error();

        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            return error();
        }

    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request the request
     * @param session the session
     * @param name    the attribute name
     */
    private void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session, final String name) {
        final String value = (String) session.getAttribute(name);
        request.setAttribute(name, value);
    }

    /**
     * Save a request parameter in the web session.
     *
     * @param request the request
     * @param session the session
     * @param name    the attribute name
     */
    private void saveRequestParameter(final HttpServletRequest request, final HttpSession session, final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }

    /**
     * set the CAS config.
     *
     * @param centralAuthenticationService the cas config
     */
    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * sets the WsFederation configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(final WsFederationConfiguration configuration) {
        this.configuration = configuration;
    }
}
