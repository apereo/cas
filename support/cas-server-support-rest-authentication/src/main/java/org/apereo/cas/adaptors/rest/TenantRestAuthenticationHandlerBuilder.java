package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.rest.RestAuthenticationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
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
    private final HttpClient httpClient;

    @Override
    public List<AuthenticationHandler> buildInternal(final TenantDefinition tenantDefinition,
                                                     final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {

        if (bindingContext.containsBindingFor(RestAuthenticationProperties.class)) {
            val casProperties = bindingContext.value();
            return casProperties
                .getAuthn()
                .getRest()
                .stream()
                .map(prop -> {
                    val handler = new RestAuthenticationHandler(
                        principalFactory, prop, applicationContext, httpClient);
                    return handler.markDisposable();
                })
                .toList();
        }
        return List.of();
    }
}

