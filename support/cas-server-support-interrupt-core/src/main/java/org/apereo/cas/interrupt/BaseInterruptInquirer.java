package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.WebBasedRegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class BaseInterruptInquirer implements InterruptInquirer {
    @Override
    public final InterruptResponse inquire(final Authentication authentication,
                                           final RegisteredService registeredService,
                                           final Service service,
                                           final Credential credential,
                                           final RequestContext requestContext) throws Throwable {
        if (shouldSkipInterruptForRegisteredService(registeredService)) {
            return InterruptResponse.none();
        }
        return inquireInternal(authentication, registeredService, service, credential, requestContext);
    }

    /**
     * Should skip interrupt for registered service.
     *
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean shouldSkipInterruptForRegisteredService(final RegisteredService registeredService) {
        if (registeredService != null) {
            LOGGER.trace("Checking interrupt rules for service [{}]", registeredService.getName());
            val policy = ((WebBasedRegisteredService) registeredService).getWebflowInterruptPolicy();
            if (policy != null && !policy.isEnabled()) {
                LOGGER.debug("Service [{}] is assigned an interrupt policy that disables interrupt notifications", registeredService.getName());
                return true;
            }
            LOGGER.debug("Service [{}] is set to allow interrupt notifications", registeredService.getName());
        } else {
            LOGGER.trace("No service was found in the request context to trigger interrupt. Proceeding as usual...");
        }
        return false;
    }

    protected abstract InterruptResponse inquireInternal(Authentication authentication,
                                                         RegisteredService registeredService,
                                                         Service service,
                                                         Credential credential,
                                                         RequestContext requestContext) throws Throwable;
}
