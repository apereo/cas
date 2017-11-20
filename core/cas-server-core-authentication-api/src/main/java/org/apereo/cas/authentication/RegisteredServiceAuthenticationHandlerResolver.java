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
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolver}
 * that acts on the criteria presented by a registered service to
 * detect which handler(s) should be resolved for authentication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceAuthenticationHandlerResolver.class);
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Instantiates a new Registered service authentication handler resolver.
     *
     * @param servicesManager the services manager
     */
    public RegisteredServiceAuthenticationHandlerResolver(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers,
                                              final AuthenticationTransaction transaction) {
        final Service service = transaction.getService();
        if (service != null && this.servicesManager != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", registeredService);
                throw new UnauthorizedSsoServiceException();
            }
            if (!registeredService.getRequiredHandlers().isEmpty()) {
                LOGGER.debug("Authentication transaction requires [{}] for service [{}]", registeredService.getRequiredHandlers(), service);
                final Set<AuthenticationHandler> handlerSet = new LinkedHashSet<>(candidateHandlers);
                LOGGER.info("Candidate authentication handlers examined this transaction are [{}]", handlerSet);

                final Iterator<AuthenticationHandler> it = handlerSet.iterator();
                while (it.hasNext()) {
                    final AuthenticationHandler handler = it.next();
                    if (!(handler instanceof HttpBasedServiceCredentialsAuthenticationHandler)
                            && !registeredService.getRequiredHandlers().contains(handler.getName())) {
                        LOGGER.debug("Authentication handler [{}] is not required for this transaction and is removed", handler.getName());
                        it.remove();
                    }
                }
                LOGGER.debug("Authentication handlers for this transaction are [{}]", handlerSet);
                return handlerSet;
            }
            LOGGER.debug("No specific authentication handlers are required for this transaction");
        }

        final String handlers = candidateHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(","));
        LOGGER.debug("Authentication handlers used for this transaction are [{}]", handlers);
        return candidateHandlers;
    }
}
