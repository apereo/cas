package org.apereo.cas.syncope;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
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
    protected final PasswordPolicyContext passwordPolicyConfiguration;
    protected final PrincipalFactory principalFactory;
    protected final ConfigurableApplicationContext applicationContext;
    protected final ServicesManager servicesManager;

    @Override
    public List<? extends AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                               final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.isBound() && bindingContext.containsBindingFor(SyncopeAuthenticationProperties.class)) {
            val casProperties = bindingContext.value();
            val syncope = casProperties.getAuthn().getSyncope();
            val handlers = SyncopeUtils.newAuthenticationHandlers(syncope, applicationContext,
                principalFactory, servicesManager, passwordPolicyConfiguration);
            handlers.forEach(AuthenticationHandler::markDisposable);
            return handlers;
        }
        return List.of();
    }

}
