package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.overview.PalantirApplicationChanges;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.version.EntityHistoryRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets Palantir's home page list the latest changes to applications from the service change history, which every CAS
 * server shares, when that module is installed. The history is looked up when it is asked for, so Palantir keeps
 * working with its own list when the history is turned off.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Palantir)
@ConditionalOnClass(name = "org.apereo.cas.version.EntityHistoryRepository")
@Configuration(value = "CasPalantirServiceHistoryConfiguration", proxyBeanMethods = false)
class CasPalantirServiceHistoryConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "palantirApplicationChanges")
    public PalantirApplicationChanges palantirApplicationChanges(
        @Qualifier("objectVersionRepository")
        final ObjectProvider<EntityHistoryRepository> objectVersionRepository) {
        return limit -> Optional.ofNullable(objectVersionRepository.getIfAvailable())
            .map(repository -> repository.getRecentChanges(limit)
                .stream()
                .map(change -> new PalantirApplicationChanges.Change(change.id(), change.name(), change.type(),
                    change.action(), change.author(), change.when()))
                .toList());
    }
}
