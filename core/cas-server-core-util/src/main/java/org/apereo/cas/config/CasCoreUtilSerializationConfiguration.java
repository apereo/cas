package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.util.serialization.DefaultComponentSerializationPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * This is {@link CasCoreUtilSerializationConfiguration}.
 * It also by default acts as a vanilla serialization plan configurator that does nothing
 * in order to satisfy the auto-wiring requirements.
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casCoreUtilSerializationConfiguration")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CasCoreUtilSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreUtilSerializationConfiguration.class);

    @ConditionalOnMissingBean(name = "componentSerializationPlan")
    @Autowired
    @Bean
    public ComponentSerializationPlan componentSerializationPlan(final List<ComponentSerializationPlanConfigurator> configurers) {
        final DefaultComponentSerializationPlan plan = new DefaultComponentSerializationPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring component serialization plan [{}]", name);
            c.configureComponentSerializationPlan(plan);
        });
        return plan;
    }
}
