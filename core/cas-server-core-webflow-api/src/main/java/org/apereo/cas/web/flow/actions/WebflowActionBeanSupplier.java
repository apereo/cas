package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Action;

import java.util.function.Supplier;

/**
 * This is {@link WebflowActionBeanSupplier}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder(setterPrefix = "with")
@Slf4j
public class WebflowActionBeanSupplier implements Supplier<Action> {

    private final ApplicationContext applicationContext;

    private final CasConfigurationProperties properties;

    @Builder.Default
    private final BeanCondition condition = BeanCondition.alwaysTrue();
    
    private final Supplier<Action> action;

    private final String id;

    @Override
    public Action get() {
        return BeanSupplier.of(Action.class)
            .ifExists(properties.getWebflow().getGroovy().getActions().get(id))
            .and(condition.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val script = properties.getWebflow().getGroovy().getActions().get(id);
                LOGGER.debug("Locating action Groovy script from [{}] for id [{}]", script, id);
                val resource = ResourceUtils.getRawResourceFrom(script);
                val watchableResource = new WatchableGroovyScriptResource(resource);
                return new GroovyScriptWebflowAction(watchableResource, applicationContext, properties);
            }))
            .otherwise(action)
            .get();
    }
}
