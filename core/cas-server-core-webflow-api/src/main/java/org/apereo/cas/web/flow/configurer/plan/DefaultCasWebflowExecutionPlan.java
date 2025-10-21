package org.apereo.cas.web.flow.configurer.plan;

import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerInterceptor;
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
@RequiredArgsConstructor
public class DefaultCasWebflowExecutionPlan implements CasWebflowExecutionPlan {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final List<CasWebflowConfigurer> webflowConfigurers = new ArrayList<>();

    private final List<HandlerInterceptor> webflowInterceptors = new ArrayList<>();

    private final List<CasWebflowLoginContextProvider> webflowLoginContextProviders = new ArrayList<>();

    private final ConfigurableApplicationContext applicationContext;

    private boolean initialized;

    @Override
    public void registerWebflowLoginContextProvider(final CasWebflowLoginContextProvider provider) {
        if (BeanSupplier.isNotProxy(provider)) {
            LOGGER.trace("Registering webflow login context provider [{}]", provider.getName());
            this.webflowLoginContextProviders.add(provider);
        }
    }

    @Override
    public void registerWebflowConfigurer(final CasWebflowConfigurer cfg) {
        if (BeanSupplier.isNotProxy(cfg)) {
            LOGGER.trace("Registering webflow configurer [{}]", cfg.getName());
            this.webflowConfigurers.add(cfg);
        }
    }

    @Override
    public void registerWebflowInterceptor(final HandlerInterceptor interceptor) {
        if (BeanSupplier.isNotProxy(interceptor)) {
            LOGGER.trace("Registering webflow interceptor [{}]", interceptor.getClass().getSimpleName());
            this.webflowInterceptors.add(interceptor);
        }
    }

    @Override
    public CasWebflowExecutionPlan execute() {
        lock.tryLock(_ -> {
            if (!initialized) {
                AnnotationAwareOrderComparator.sortIfNecessary(webflowConfigurers);
                AnnotationAwareOrderComparator.sortIfNecessary(webflowLoginContextProviders);
                webflowConfigurers
                    .stream()
                    .filter(BeanSupplier::isNotProxy)
                    .forEach(cfg -> {
                        LOGGER.trace("Registering webflow configurer [{}]", cfg.getName());
                        cfg.initialize();
                    });
                webflowConfigurers
                    .stream()
                    .filter(BeanSupplier::isNotProxy)
                    .forEach(cfg -> cfg.postInitialization(applicationContext));
                initialized = true;
            }
        });
        return this;
    }

}
