package org.apereo.cas.syncope;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;

/**
 * This is {@link TenantSyncopeAuthenticationHandlerBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantSyncopeAuthenticationHandlerBuilder implements TenantAuthenticationHandlerBuilder {
    private final PasswordPolicyContext passwordPolicyConfiguration;
    private final PrincipalFactory principalFactory;
    private final ConfigurableApplicationContext applicationContext;
    private final ServicesManager servicesManager;

    @Override
    public List<? extends AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                               final CasConfigurationProperties casProperties) {
        val syncope = casProperties.getAuthn().getSyncope();
        val handlers = SyncopeUtils.newAuthenticationHandlers(syncope, applicationContext,
            principalFactory, servicesManager, passwordPolicyConfiguration);
        handlers.forEach(AuthenticationHandler::markDisposable);
        return handlers;
    }

}
