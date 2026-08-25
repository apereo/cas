package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aup.MongoDbAcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TenantMongoDbAcceptableUsagePolicyRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class TenantMongoDbAcceptableUsagePolicyRepositoryBuilder implements TenantAcceptableUsagePolicyRepositoryBuilder {
    private final CasSSLContext casSslContext;

    @Override
    public @Nullable AcceptableUsagePolicyRepository buildInternal(final TenantDefinition tenantDefinition,
                                                                   final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor(MongoDbAcceptableUsagePolicyProperties.class)) {
            val casProperties = bindingContext.value();
            val mongo = casProperties.getAcceptableUsagePolicy().getMongo();
            val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
            val mongoTemplate = factory.buildMongoTemplate(mongo);
            MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
            return new MongoDbAcceptableUsagePolicyRepository(casProperties.getAcceptableUsagePolicy(), mongoTemplate);
        }
        return null;
    }
}
