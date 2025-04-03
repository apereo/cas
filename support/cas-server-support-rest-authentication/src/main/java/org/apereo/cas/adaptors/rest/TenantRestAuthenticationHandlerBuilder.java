package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;

/**
 * This is {@link TenantRestAuthenticationHandlerBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantRestAuthenticationHandlerBuilder implements TenantAuthenticationHandlerBuilder {
    private final ConfigurableApplicationContext applicationContext;
    private final PrincipalFactory principalFactory;
    private final ServicesManager servicesManager;
    private final HttpClient httpClient;

    @Override
    public List<AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                     final CasConfigurationProperties casProperties) {
        return casProperties
            .getAuthn()
            .getRest()
            .stream()
            .map(prop -> {
                val handler = new RestAuthenticationHandler(servicesManager,
                    principalFactory, prop, applicationContext, httpClient);
                return handler.markDisposable();
            })
            .toList();
    }
}

