package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolver}
 * that acts on the criteria presented by a registered service to
 * detect which handler(s) should be resolved for authentication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers,
                                              final AuthenticationTransaction transaction) {
        final Service service = transaction.getService();
        if (service != null && this.servicesManager != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                logger.warn("Service [{}] is not allowed to use SSO.", registeredService);
                throw new UnauthorizedSsoServiceException();
            }
            if (!registeredService.getRequiredHandlers().isEmpty()) {
                logger.debug("Authentication transaction requires {} for service {}", registeredService.getRequiredHandlers(), service);
                final Set<AuthenticationHandler> handlerSet = new LinkedHashSet<>(candidateHandlers);
                logger.info("Candidate authentication handlers examined this transaction are {}", handlerSet);

                final Iterator<AuthenticationHandler> it = handlerSet.iterator();
                while (it.hasNext()) {
                    final AuthenticationHandler handler = it.next();
                    if (!(handler instanceof HttpBasedServiceCredentialsAuthenticationHandler)
                            && !registeredService.getRequiredHandlers().contains(handler.getName())) {
                        logger.debug("Authentication handler {} is not required for this transaction and is removed", handler.getName());
                        it.remove();
                    }
                }
                logger.debug("Authentication handlers for this transaction are {}", handlerSet);
                return handlerSet;
            } else {
                logger.debug("No specific authentication handlers are required for this transaction");
            }
        }

        logger.debug("Authentication handlers used for this transaction are {}", candidateHandlers);
        return candidateHandlers;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
