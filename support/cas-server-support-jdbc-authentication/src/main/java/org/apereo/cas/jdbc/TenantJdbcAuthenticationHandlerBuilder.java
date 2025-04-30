package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BindJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.ProcedureJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link TenantJdbcAuthenticationHandlerBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantJdbcAuthenticationHandlerBuilder implements TenantAuthenticationHandlerBuilder {
    private final PasswordPolicyContext passwordPolicyConfiguration;
    private final PrincipalFactory jdbcPrincipalFactory;
    private final ConfigurableApplicationContext applicationContext;
    private final ServicesManager servicesManager;

    @Override
    public List<AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                     final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        val handlers = new ArrayList<AuthenticationHandler>();
        val jdbc = bindingContext.value().getAuthn().getJdbc();
        if (bindingContext.containsBindingFor(BindJdbcAuthenticationProperties.class)) {
            createHandler(jdbc.getBind(), handlers);
        }
        if (bindingContext.containsBindingFor(QueryJdbcAuthenticationProperties.class)) {
            createHandler(jdbc.getQuery(), handlers);
        }
        if (bindingContext.containsBindingFor(QueryEncodeJdbcAuthenticationProperties.class)) {
            createHandler(jdbc.getEncode(), handlers);
        }
        if (bindingContext.containsBindingFor(SearchJdbcAuthenticationProperties.class)) {
            createHandler(jdbc.getSearch(), handlers);
        }
        if (bindingContext.containsBindingFor(ProcedureJdbcAuthenticationProperties.class)) {
            createHandler(jdbc.getProcedure(), handlers);
        }
        return handlers;
    }

    protected void createHandler(
        final List<? extends BaseJdbcAuthenticationProperties> container,
        final List<AuthenticationHandler> finalHandlers) {
        container.forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
                jdbcPrincipalFactory, servicesManager, passwordPolicyConfiguration);
            finalHandlers.add(handler.markDisposable());
        });
    }
}
