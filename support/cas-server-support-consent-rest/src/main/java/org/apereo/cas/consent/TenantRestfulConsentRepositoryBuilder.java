package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.consent.RestfulConsentProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.util.http.HttpClient;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link TenantRestfulConsentRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class TenantRestfulConsentRepositoryBuilder implements TenantConsentRepositoryBuilder {
    private final HttpClient httpClient;

    @Override
    public List<ConsentRepository> buildInternal(
        final TenantDefinition tenantDefinition,
        final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        return bindingContext.containsBindingFor(RestfulConsentProperties.class)
            ? List.of(new RestfulConsentRepository(bindingContext.value().getConsent().getRest(), httpClient).markDisposable())
            : List.of();
    }
}
