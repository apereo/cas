package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@RequiredArgsConstructor
@Getter
@Setter
public class RegisteredServiceAuthenticationHandlerResolver implements AuthenticationHandlerResolver {

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The service selection plan.
     */
    protected final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan;

    private int order;

    private static Set<AuthenticationHandler> filterExcludedAuthenticationHandlers(
        final Set<AuthenticationHandler> candidateHandlers,
        final Service service,
        final RegisteredService registeredService) {

        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        val excludedHandlers = authenticationPolicy.getExcludedAuthenticationHandlers();
        LOGGER.debug("Authentication transaction excludes [{}] for service [{}]", excludedHandlers, service);

        val handlerSet = new LinkedHashSet<>(candidateHandlers);
        LOGGER.debug("Candidate authentication handlers examined for this transaction are [{}]", handlerSet);

        if (!excludedHandlers.isEmpty()) {
            val it = handlerSet.iterator();
            while (it.hasNext()) {
                val handler = it.next();
                val handlerName = handler.getName();
                if (excludedHandlers.contains(handlerName)) {
                    LOGGER.debug("Authentication handler [{}] is excluded for this transaction and is removed", handlerName);
                    it.remove();
                }
            }
        }
        LOGGER.info("Final authentication handlers after exclusion rules are [{}]", handlerSet);
        return handlerSet;
    }

    private static Set<AuthenticationHandler> filterRequiredAuthenticationHandlers(
        final Set<AuthenticationHandler> candidateHandlers,
        final Service service, final RegisteredService registeredService) {

        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        val requiredHandlers = authenticationPolicy.getRequiredAuthenticationHandlers();
        LOGGER.debug("Authentication transaction requires [{}] for service [{}]", requiredHandlers, service);
        val handlerSet = new LinkedHashSet<>(candidateHandlers);
        LOGGER.debug("Candidate authentication handlers examined for this transaction are [{}]", handlerSet);

        if (!requiredHandlers.isEmpty()) {
            val it = handlerSet.iterator();
            while (it.hasNext()) {
                val handler = it.next();
                val handlerName = handler.getName();
                val removeHandler = !(handler instanceof MultifactorAuthenticationHandler)
                    && !(handler instanceof HttpBasedServiceCredentialsAuthenticationHandler)
                    && !requiredHandlers.contains(handlerName);
                if (removeHandler) {
                    it.remove();
                    LOGGER.debug("Authentication handler [{}] is removed", handlerName);
                }
            }
        }
        LOGGER.info("Final authentication handlers after inclusion rules are [{}]", handlerSet);
        return handlerSet;
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers,
                                              final AuthenticationTransaction transaction) {
        val service = authenticationServiceSelectionPlan.resolveService(transaction.getService());
        val registeredService = this.servicesManager.findServiceBy(service);

        val requiredHandlers = filterRequiredAuthenticationHandlers(candidateHandlers, service, registeredService);
        return filterExcludedAuthenticationHandlers(requiredHandlers, service, registeredService);
    }

    @Override
    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        val service = authenticationServiceSelectionPlan.resolveService(transaction.getService());
        if (service != null) {
            val registeredService = this.servicesManager.findServiceBy(service);
            LOGGER.trace("Located registered service definition [{}] for this authentication transaction", registeredService);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", service);
                throw new UnauthorizedSsoServiceException();
            }
            val authenticationPolicy = registeredService.getAuthenticationPolicy();
            return !authenticationPolicy.getRequiredAuthenticationHandlers().isEmpty()
                || !authenticationPolicy.getExcludedAuthenticationHandlers().isEmpty();
        }
        return false;
    }
}
