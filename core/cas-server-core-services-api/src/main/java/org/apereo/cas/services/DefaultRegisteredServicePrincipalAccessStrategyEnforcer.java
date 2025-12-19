package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.PrincipalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link DefaultRegisteredServicePrincipalAccessStrategyEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRegisteredServicePrincipalAccessStrategyEnforcer implements RegisteredServicePrincipalAccessStrategyEnforcer {
    private final ApplicationContext applicationContext;

    @Override
    public Boolean authorize(final PrincipalAccessStrategyContext context) {
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(context.getService(), context.getRegisteredService());
        val serviceId = context.getService() != null ? context.getService().getId() : "unknown";
        LOGGER.trace("Checking access strategy for service [{}], requested by [{}] with attributes [{}].", serviceId, context.getPrincipalId(), context.getPrincipalAttributes());
        val accessRequest = RegisteredServiceAccessStrategyRequest.builder()
            .service(context.getService())
            .principalId(context.getPrincipalId())
            .attributes(context.getPrincipalAttributes())
            .registeredService(context.getRegisteredService())
            .applicationContext(this.applicationContext)
            .build();
        if (Unchecked.supplier(() -> !context.getRegisteredService().getAccessStrategy().authorizeRequest(accessRequest)).get()) {
            LOGGER.warn("Cannot grant access to service [{}]; it is not authorized for use by [{}].", serviceId, context.getPrincipalId());
            val handlerErrors = new HashMap<String, Throwable>();
            val message = String.format("Cannot authorize principal %s to access service %s, likely due to insufficient permissions", context.getPrincipalId(), serviceId);
            val exception = new UnauthorizedServiceForPrincipalException(message, context.getRegisteredService(), context.getPrincipalId(), context.getPrincipalAttributes());
            handlerErrors.put(UnauthorizedServiceForPrincipalException.class.getSimpleName(), exception);
            throw new PrincipalException(message, handlerErrors, new HashMap<>());
        }
        return true;
    }


}
