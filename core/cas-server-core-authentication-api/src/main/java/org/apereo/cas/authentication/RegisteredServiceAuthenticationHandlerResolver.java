package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;

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
@Slf4j
@AllArgsConstructor
public class RegisteredServiceAuthenticationHandlerResolver implements AuthenticationHandlerResolver {

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    @Override
    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        final var service = transaction.getService();
        if (service != null) {
            final var registeredService = this.servicesManager.findServiceBy(service);
            LOGGER.debug("Located registered service definition [{}] for this authentication transaction", registeredService);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", registeredService);
                throw new UnauthorizedSsoServiceException();
            }
            return !registeredService.getRequiredHandlers().isEmpty();
        }
        return false;
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        final var service = transaction.getService();
        final var registeredService = this.servicesManager.findServiceBy(service);

        final var requiredHandlers = registeredService.getRequiredHandlers();
        LOGGER.debug("Authentication transaction requires [{}] for service [{}]", requiredHandlers, service);
        final Set<AuthenticationHandler> handlerSet = new LinkedHashSet<>(candidateHandlers);
        LOGGER.info("Candidate authentication handlers examined this transaction are [{}]", handlerSet);

        final var it = handlerSet.iterator();
        while (it.hasNext()) {
            final var handler = it.next();
            final var handlerName = handler.getName();
            if (!(handler instanceof HttpBasedServiceCredentialsAuthenticationHandler) && !requiredHandlers.contains(handlerName)) {
                LOGGER.debug("Authentication handler [{}] is not required for this transaction and is removed", handlerName);
                it.remove();
            }
        }
        LOGGER.debug("Authentication handlers for this transaction are [{}]", handlerSet);
        return handlerSet;

    }
}
