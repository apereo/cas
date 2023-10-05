package org.apereo.cas.pac4j.client.authz;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Client;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseDelegatedClientIdentityProviderAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseDelegatedClientIdentityProviderAuthorizer implements DelegatedClientIdentityProviderAuthorizer {
    private final ServicesManager servicesManager;

    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;


    @Override
    public boolean isDelegatedClientAuthorizedFor(final String clientName, final Service service,
                                                  final RequestContext context) throws Throwable {
        return handleAuthorizationForService(clientName, service);
    }

    @Override
    public boolean isDelegatedClientAuthorizedFor(final String clientName, final Service service,
                                                  final HttpServletRequest request) throws Throwable {
        return handleAuthorizationForService(clientName, service);
    }

    protected boolean handleAuthorizationForService(final String clientName, final Service service) throws Throwable {
        if (service == null || StringUtils.isBlank(service.getId())) {
            LOGGER.debug("Can not evaluate delegated authentication policy without a service");
            return true;
        }
        if (StringUtils.isBlank(clientName)) {
            LOGGER.debug("No client is provided to enforce authorization for delegated authentication. SSO session "
                         + "may have been established without delegated authentication");
            return true;
        }
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            LOGGER.warn("Service access for [{}] is denied", registeredService);
            return false;
        }
        LOGGER.trace("Located registered service definition [{}] matching [{}]", registeredService, service);
        val auditContext = AuditableContext.builder()
            .registeredService(registeredService)
            .service(service)
            .properties(CollectionUtils.wrap(Client.class.getSimpleName(), clientName))
            .build();
        val result = delegatedAuthenticationPolicyEnforcer.execute(auditContext);
        if (!result.isExecutionFailure()) {
            LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, clientName);
            return true;
        }
        LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), clientName);
        return false;
    }
}
