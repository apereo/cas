package org.apereo.cas.config;

import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.serialization.DefaultComponentSerializationPlan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * This is {@link CasCoreUtilSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCoreUtilSerializationConfiguration", proxyBeanMethods = true)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CasCoreUtilSerializationConfiguration {

    @Autowired
    private ObjectProvider<List<ComponentSerializationPlanConfigurer>> configurers;

    @ConditionalOnMissingBean(name = "componentSerializationPlan")
    @Bean
    public ComponentSerializationPlan componentSerializationPlan() {
        val plan = new DefaultComponentSerializationPlan();
        configurers.ifAvailable(cfgs -> {
            cfgs.forEach(c -> {
                LOGGER.trace("Configuring component serialization plan [{}]", c.getName());
                c.configureComponentSerializationPlan(plan);
            });
        });

        return plan;
    }
}
