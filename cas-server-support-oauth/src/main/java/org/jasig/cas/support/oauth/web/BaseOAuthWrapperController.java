package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * It finds the right sub controller to call according to the url.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public abstract class BaseOAuthWrapperController extends AbstractController {

    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /** The ticket registry. */
    @NotNull
    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    /**
     * Check if a parameter exists.
     *
     * @param request the HTTP request
     * @param name the parameter name
     * @return whether the parameter exists
     */
    protected boolean checkParameterExist(final HttpServletRequest request, final String name) {
        final String parameter = request.getParameter(name);
        logger.debug("{}: {}", name, parameter);
        if (StringUtils.isBlank(parameter)) {
            logger.error("Missing: {}", name);
            return false;
        }
        return true;
    }

    /**
     * Check if the service is valid.
     *
     * @param request the HTTP request
     * @return whether the service is valid
     */
    protected boolean checkServiceValid(final HttpServletRequest request) {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        logger.debug("Found: {} for: {}", service, clientId);
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            logger.error("Service {} is not found in the registry or it is disabled.", clientId);
            return false;
        }
        return true;
    }


    /**
     * Check if the callback url is valid.
     *
     * @param request the HTTP request
     * @return whether the callback url is valid
     */
    protected boolean checkCallbackValid(final HttpServletRequest request) {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        final String serviceId = service.getServiceId();
        logger.debug("Found: {} for: {} vs redirectUri: {}", service, clientId, redirectUri);
        if (!redirectUri.matches(serviceId)) {
            logger.error("Unsupported {}: {} for serviceId: {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return false;
        }
        return true;
    }


    /**
     * Check the client secret.
     *
     * @param request the HTTP request
     * @return whether the secret is valid
     */
    protected boolean checkClientSecret(final HttpServletRequest request) {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        logger.debug("Found: {} for: {} in secret check", service, clientId);
        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service: {}", service);
            return false;
        }
        return true;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }
}
