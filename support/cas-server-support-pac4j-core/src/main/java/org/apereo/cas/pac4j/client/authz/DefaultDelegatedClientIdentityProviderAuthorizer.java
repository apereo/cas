package org.apereo.cas.pac4j.client.authz;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class DefaultDelegatedClientIdentityProviderAuthorizer extends BaseDelegatedClientIdentityProviderAuthorizer {

    public DefaultDelegatedClientIdentityProviderAuthorizer(final ServicesManager servicesManager,
                                                            final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                                            final TenantExtractor tenantExtractor) {
        super(servicesManager, delegatedAuthenticationPolicyEnforcer, tenantExtractor);
    }

}
