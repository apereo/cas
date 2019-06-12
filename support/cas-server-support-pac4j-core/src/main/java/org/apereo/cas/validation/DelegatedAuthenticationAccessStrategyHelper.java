package org.apereo.cas.validation;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Client;

import java.util.ArrayList;

/**
 * This is {@link DelegatedAuthenticationAccessStrategyHelper}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationAccessStrategyHelper {
    private final ServicesManager servicesManager;
    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    /**
     * Is delegated client authorized for service boolean.
     *
     * @param client  the client
     * @param service the service
     * @return the boolean
     */
    public boolean isDelegatedClientAuthorizedForService(final Client client, final Service service) {
        return isDelegatedClientAuthorizedFor(client.getName(), service);
    }

    /**
     * Is delegated client authorized for authentication?.
     *
     * @param authentication the authentication
     * @param service        the service
     * @return the boolean
     */
    public boolean isDelegatedClientAuthorizedForAuthentication(final Authentication authentication,
                                                                final Service service) {
        val clientName = getClientNameFromAuthentication(authentication);
        return isDelegatedClientAuthorizedFor(clientName, service);
    }

    /**
     * Gets client name from authentication.
     *
     * @param authentication the authentication
     * @return the client name from authentication
     */
    public static String getClientNameFromAuthentication(final Authentication authentication) {
        val clientNames = authentication.getAttributes()
            .getOrDefault(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, new ArrayList<>());
        return CollectionUtils.firstElement(clientNames).map(Object::toString).orElse(StringUtils.EMPTY);
    }

    /**
     * Is delegated client authorized for boolean.
     *
     * @param clientName the client name
     * @param service    the service
     * @return the boolean
     */
    public boolean isDelegatedClientAuthorizedFor(final String clientName, final Service service) {
        if (service == null || StringUtils.isBlank(service.getId())) {
            LOGGER.debug("Can not evaluate delegated authentication policy without a service");
            return true;
        }

        if (StringUtils.isBlank(clientName)) {
            LOGGER.debug("No client is provided to enforce authorization for delegated authentication. SSO session "
                + "may have been established without delegated authentication");
            return true;
        }

        val registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("Service access for [{}] is denied", registeredService);
            return false;
        }
        LOGGER.trace("Located registered service definition [{}] matching [{}]", registeredService, service);
        val context = AuditableContext.builder()
            .registeredService(registeredService)
            .properties(CollectionUtils.wrap(Client.class.getSimpleName(), clientName))
            .build();
        val result = delegatedAuthenticationPolicyEnforcer.execute(context);
        if (!result.isExecutionFailure()) {
            LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, clientName);
            return true;
        }
        LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), clientName);
        return false;
    }
}
