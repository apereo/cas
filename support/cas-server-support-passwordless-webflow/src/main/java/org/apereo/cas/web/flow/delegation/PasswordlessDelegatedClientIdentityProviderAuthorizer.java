package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.client.authz.BaseDelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordlessDelegatedClientIdentityProviderAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class PasswordlessDelegatedClientIdentityProviderAuthorizer extends BaseDelegatedClientIdentityProviderAuthorizer {
    public PasswordlessDelegatedClientIdentityProviderAuthorizer(final ServicesManager servicesManager,
                                                                 final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                                                 final TenantExtractor tenantExtractor) {
        super(servicesManager, delegatedAuthenticationPolicyEnforcer, tenantExtractor);
    }

    @Override
    public boolean isDelegatedClientAuthorizedFor(final String clientName, final Service service,
                                                  final RequestContext requestContext) throws Throwable {
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        return account == null
            || (super.isDelegatedClientAuthorizedFor(clientName, service, requestContext)
            && (account.getAllowedDelegatedClients().isEmpty() || account.getAllowedDelegatedClients().contains(clientName)));
    }
}
