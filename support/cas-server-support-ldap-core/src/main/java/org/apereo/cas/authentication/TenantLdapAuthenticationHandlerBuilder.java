package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;

/**
 * This is {@link TenantLdapAuthenticationHandlerBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantLdapAuthenticationHandlerBuilder implements TenantAuthenticationHandlerBuilder {
    private final ConfigurableApplicationContext applicationContext;
    private final PrincipalFactory ldapPrincipalFactory;
    private final ServicesManager servicesManager;

    @Override
    public List<AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                     final CasConfigurationProperties casProperties) {
        return casProperties
            .getAuthn()
            .getLdap()
            .stream()
            .filter(LdapUtils::isLdapAuthenticationConfigured)
            .map(prop -> {
                val handler = LdapUtils.createLdapAuthenticationHandler(prop,
                    applicationContext, servicesManager, ldapPrincipalFactory);
                handler.setState(prop.getState());
                LOGGER.info("Created LDAP authentication handler [{}] with state [{}]",
                    handler.getName(), handler.getState());
                return handler.markDisposable();
            })
            .toList();
    }
}
