package org.jasig.cas.support.oauth.validator;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * Validate OAuth inputs.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Component("oAuthValidator")
public class OAuthValidator {

    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /**
     * Check if a parameter exists.
     *
     * @param request the HTTP request
     * @param name the parameter name
     * @return whether the parameter exists
     */
    public boolean checkParameterExist(final HttpServletRequest request, final String name) {
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
     * @param clientId the client identifier
     * @return whether the service is valid
     */
    public boolean checkServiceValid(final String clientId) {
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
     * @param clientId the client identifier
     * @param redirectUri the callback url
     * @return whether the callback url is valid
     */
    public boolean checkCallbackValid(final String clientId, final String redirectUri) {
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
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
     * @param clientId the client identifier
     * @param clientSecret the client secret
     * @return whether the secret is valid
     */
    public boolean checkClientSecret(final String clientId, final String clientSecret) {
        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        logger.debug("Found: {} for: {} in secret check", service, clientId);

        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service: {}", service);
            return false;
        }
        return true;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
