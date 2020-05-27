package org.apereo.cas.web.flow.configurer.plan;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultCasWebflowExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
public class DefaultCasWebflowExecutionPlan implements CasWebflowExecutionPlan {
    private final List<CasWebflowConfigurer> webflowConfigurers = new ArrayList<>(0);

    @Override
    public void registerWebflowConfigurer(final CasWebflowConfigurer cfg) {
        LOGGER.trace("Registering webflow configurer [{}]", cfg.getName());
        this.webflowConfigurers.add(cfg);
    }

    @Override
    public void execute() {
        AnnotationAwareOrderComparator.sortIfNecessary(webflowConfigurers);
        webflowConfigurers.forEach(c -> {
            LOGGER.trace("Registering webflow configurer [{}]", c.getName());
            c.initialize();
        });
    }
}
