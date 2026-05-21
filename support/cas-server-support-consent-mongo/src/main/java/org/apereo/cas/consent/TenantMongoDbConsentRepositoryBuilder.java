package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.consent.MongoDbConsentProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link TenantMongoDbConsentRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class TenantMongoDbConsentRepositoryBuilder implements TenantConsentRepositoryBuilder {
    private final CasSSLContext casSslContext;

    @Override
    public List<ConsentRepository> buildInternal(
        final TenantDefinition tenantDefinition,
        final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {

        if (bindingContext.containsBindingFor(MongoDbConsentProperties.class)) {
            return List.of(MongoDbConsentRepository.from(casSslContext, bindingContext.value()));
        }
        return List.of();
    }
}
