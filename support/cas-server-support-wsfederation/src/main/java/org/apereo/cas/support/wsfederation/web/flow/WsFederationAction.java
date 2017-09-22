package org.apereo.cas.support.wsfederation.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.saml1.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(WsFederationAction.class);

    private final WsFederationHelper wsFederationHelper;
    private final WsFederationConfiguration configuration;
    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;
    private final String authorizationUrl;

    public WsFederationAction(final AuthenticationSystemSupport authenticationSystemSupport,
                              final CentralAuthenticationService centralAuthenticationService,
                              final WsFederationConfiguration wsFederationConfiguration,
                              final WsFederationHelper wsFederationHelper,
                              final ServicesManager servicesManager) {
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.centralAuthenticationService = centralAuthenticationService;
        this.configuration = wsFederationConfiguration;
        this.wsFederationHelper = wsFederationHelper;
        this.servicesManager = servicesManager;

        this.authorizationUrl = configuration.getIdentityProviderUrl() + QUERYSTRING;
    }

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
            final String wa = request.getParameter(WA);
            if (StringUtils.isNotBlank(wa) && wa.equalsIgnoreCase(WSIGNIN)) {
                return handleWsFederationAuthenticationRequest(context);
            }
            return routeToLoginRequest(context);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return error();
        }
    }

    private Event routeToLoginRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpSession session = request.getSession();

        final Service service = (Service) context.getFlowScope().get(SERVICE);
        if (service != null) {
            session.setAttribute(SERVICE, service);
        }
        saveRequestParameter(request, session, THEME);
        saveRequestParameter(request, session, LOCALE);
        saveRequestParameter(request, session, METHOD);

        final String url = authorizationUrl + getRelyingPartyIdentifier(service);

        LOGGER.info("Preparing to redirect to the IdP [{}]", url);
        context.getFlowScope().put(PROVIDERURL, url);
        LOGGER.debug("Returning error event");
        return error();
    }

    private Event handleWsFederationAuthenticationRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        final String wResult = request.getParameter(WRESULT);
        LOGGER.debug("Parameter [{}] received: [{}]", WRESULT, wResult);

        if (StringUtils.isBlank(wResult)) {
            LOGGER.error("No [{}] parameter is found", WRESULT);
            return error();
        }

        // create credentials
        LOGGER.debug("Attempting to create an assertion from the token parameter");
        final Assertion assertion = this.wsFederationHelper.parseTokenFromString(wResult, configuration);

        if (assertion == null) {
            LOGGER.error("Could not validate assertion via parsing the token from [{}]", WRESULT);
            return error();
        }

        LOGGER.debug("Attempting to validate the signature on the assertion");
        if (!this.wsFederationHelper.validateSignature(assertion, this.configuration)) {
            LOGGER.error("WS Requested Security Token is blank or the signature is not valid.");
            return error();
        }

        return buildCredentialsFromAssertion(context, assertion);
    }

    private Event buildCredentialsFromAssertion(final RequestContext context, final Assertion assertion) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            final HttpSession session = request.getSession();

            final Service service = (Service) session.getAttribute(SERVICE);

            LOGGER.debug("Creating credential based on the provided assertion");
            final WsFederationCredential credential = this.wsFederationHelper.createCredentialFromToken(assertion);

            final String rpId = getRelyingPartyIdentifier(service);
            if (credential != null && credential.isValid(rpId,
                    this.configuration.getIdentityProviderIdentifier(),
                    this.configuration.getTolerance())) {

                LOGGER.debug("Validated assertion for the created credential successfully");
                if (this.configuration.getAttributeMutator() != null) {
                    LOGGER.debug("Modifying credential attributes based on [{}]", this.configuration.getAttributeMutator().getClass().getSimpleName());
                    this.configuration.getAttributeMutator().modifyAttributes(credential.getAttributes());
                }
            } else {
                LOGGER.warn("SAML assertions are blank or no longer valid based on RP identifier [{}] and IdP identifier [{}]",
                        rpId, this.configuration.getIdentityProviderIdentifier());

                final String url = authorizationUrl + rpId;
                context.getFlowScope().put(PROVIDERURL, url);
                LOGGER.warn("Created authentication url [{}] and returning error", url);
                return error();
            }

            context.getFlowScope().put(SERVICE, service);
            restoreRequestAttribute(request, session, THEME);
            restoreRequestAttribute(request, session, LOCALE);
            restoreRequestAttribute(request, session, METHOD);

            LOGGER.debug("Creating final authentication result based on the given credential");
            final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

            LOGGER.debug("Attempting to create a ticket-granting ticket for the authentication result");
            WebUtils.putTicketGrantingTicketInScopes(context,
                    this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult));

            LOGGER.info("Token validated and new [{}] created: [{}]", credential.getClass().getName(), credential);
            return success();

        } catch (final AbstractTicketException e) {
            LOGGER.error(e.getMessage(), e);
            return error();
        }
    }

    /**
     * Get the relying party id for a service.
     *
     * @param service the service to get an id for
     * @return relying party id
     */
    private String getRelyingPartyIdentifier(final Service service) {
        String relyingPartyIdentifier = this.configuration.getRelyingPartyIdentifier();
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService.getProperties().containsKey("wsfed.relyingPartyIdentifier")) {
                relyingPartyIdentifier = registeredService.getProperties().get("wsfed.relyingPartyIdentifier").getValue();
            }
        }
        LOGGER.debug("Determined relying party identifier for [{}] to be [{}]", service, relyingPartyIdentifier);
        return relyingPartyIdentifier;
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request the request
     * @param session the session
     * @param name    the attribute name
     */
    private static void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session, final String name) {
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
    private static void saveRequestParameter(final HttpServletRequest request, final HttpSession session, final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }
}
