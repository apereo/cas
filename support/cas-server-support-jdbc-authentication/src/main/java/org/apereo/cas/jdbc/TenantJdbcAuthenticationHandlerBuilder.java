package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;
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
                                                     final CasConfigurationProperties casProperties) {
        val handlers = new ArrayList<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        createHandler(jdbc.getBind(), handlers);
        createHandler(jdbc.getQuery(), handlers);
        createHandler(jdbc.getEncode(), handlers);
        createHandler(jdbc.getSearch(), handlers);
        createHandler(jdbc.getProcedure(), handlers);
        return handlers;
    }

    private void createHandler(
        final List<? extends BaseJdbcAuthenticationProperties> container,
        final List<AuthenticationHandler> finalHandlers) {
        container.forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
                jdbcPrincipalFactory, servicesManager, passwordPolicyConfiguration);
            finalHandlers.add(handler.markDisposable());
        });
    }
}
