package org.apereo.cas.config;

import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.serialization.DefaultComponentSerializationPlan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasCoreUtilSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCoreUtilSerializationConfiguration", proxyBeanMethods = false)
@Slf4j
public class CasCoreUtilSerializationConfiguration {

    @ConditionalOnMissingBean(name = "componentSerializationPlan")
    @Bean
    @Autowired
    public ComponentSerializationPlan componentSerializationPlan(
        final ObjectProvider<List<ComponentSerializationPlanConfigurer>> configurers) {
        val plan = new DefaultComponentSerializationPlan();
        plan.registerSerializableClass(TriStateBoolean.class);

        configurers.ifAvailable(cfgs -> cfgs.forEach(c -> {
            LOGGER.trace("Configuring component serialization plan [{}]", c.getName());
            c.configureComponentSerializationPlan(plan);
        }));

        return plan;
    }
}
