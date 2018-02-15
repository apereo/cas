package org.apereo.cas.validation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.pac4j.core.client.Client;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link Pac4jServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class Pac4jServiceTicketValidationAuthorizer implements ServiceTicketValidationAuthorizer {

    private final ServicesManager servicesManager;
    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    @Override
    public void authorize(final HttpServletRequest request, final Service service, final Assertion assertion) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        LOGGER.debug("Evaluating service [{}] for delegated authentication policy", service);
        final RegisteredServiceDelegatedAuthenticationPolicy policy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
        if (policy != null) {
            final Map<String, Object> attributes = assertion.getPrimaryAuthentication().getAttributes();

            if (attributes.containsKey(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME)) {
                final Object clientNameAttr = attributes.get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME);
                final Optional<Object> value = CollectionUtils.firstElement(clientNameAttr);
                if (value.isPresent()) {
                    final String client = value.get().toString();
                    LOGGER.debug("Evaluating delegated authentication policy [{}] for client [{}] and service [{}]", policy, client, registeredService);

                    final AuditableContext context = AuditableContext.builder()
                        .registeredService(Optional.of(registeredService))
                        .properties(CollectionUtils.wrap(Client.class.getSimpleName(), client))
                        .build();
                    final AuditableExecutionResult result = delegatedAuthenticationPolicyEnforcer.execute(context);
                    result.throwExceptionIfNeeded();
                }
            }
        }
    }
}
