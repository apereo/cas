package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.serialization.DefaultComponentSerializationPlan;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreUtilSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@Configuration(value = "CasCoreUtilSerializationConfiguration", proxyBeanMethods = false)
class CasCoreUtilSerializationConfiguration {

    @ConditionalOnMissingBean(name = "componentSerializationPlan")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlan componentSerializationPlan(
        final ObjectProvider<@NonNull List<ComponentSerializationPlanConfigurer>> configurers) {
        val plan = new DefaultComponentSerializationPlan();
        plan.registerSerializableClass(TriStateBoolean.class);

        configurers.ifAvailable(cfgs -> cfgs.forEach(c -> {
            LOGGER.trace("Configuring component serialization plan [{}]", c.getName());
            c.configureComponentSerializationPlan(plan);
        }));

        return plan;
    }
}
