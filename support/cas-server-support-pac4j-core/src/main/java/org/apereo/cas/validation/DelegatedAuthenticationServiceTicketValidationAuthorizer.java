package org.apereo.cas.validation;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DelegatedAuthenticationServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationServiceTicketValidationAuthorizer implements ServiceTicketValidationAuthorizer {

    private final ServicesManager servicesManager;
    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    @Override
    public void authorize(final HttpServletRequest request, final Service service, final Assertion assertion) {
        val registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        LOGGER.debug("Evaluating service [{}] for delegated authentication policy", service);
        val policy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
        if (policy != null) {
            val attributes = assertion.getPrimaryAuthentication().getAttributes();

            if (attributes.containsKey(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME)) {
                val clientNameAttr = attributes.get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME);
                val value = CollectionUtils.firstElement(clientNameAttr);
                if (value.isPresent()) {
                    val client = value.get().toString();
                    LOGGER.debug("Evaluating delegated authentication policy [{}] for client [{}] and service [{}]", policy, client, registeredService);

                    val context = AuditableContext.builder()
                        .registeredService(registeredService)
                        .properties(CollectionUtils.wrap(Client.class.getSimpleName(), client))
                        .build();
                    val result = delegatedAuthenticationPolicyEnforcer.execute(context);
                    result.throwExceptionIfNeeded();
                }
            }
        }
    }
}
