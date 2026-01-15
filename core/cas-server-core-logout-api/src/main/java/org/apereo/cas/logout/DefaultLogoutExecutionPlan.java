package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * This is {@link DefaultLogoutExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class DefaultLogoutExecutionPlan implements LogoutExecutionPlan {
    private final List<LogoutRedirectionStrategy> logoutRedirectionStrategies = new ArrayList<>();

    private final List<LogoutPostProcessor> logoutPostProcessors = new ArrayList<>();

    private final List<SingleLogoutServiceMessageHandler> singleLogoutServiceMessageHandlers = new ArrayList<>();

    @Override
    public void registerLogoutPostProcessor(final LogoutPostProcessor handler) {
        if (BeanSupplier.isNotProxy(handler)) {
            LOGGER.debug("Registering logout handler [{}]", handler.getName());
            logoutPostProcessors.add(handler);
            AnnotationAwareOrderComparator.sort(this.logoutPostProcessors);
        }
    }

    @Override
    public void registerSingleLogoutServiceMessageHandler(final SingleLogoutServiceMessageHandler handler) {
        if (BeanSupplier.isNotProxy(handler)) {
            LOGGER.trace("Registering single logout service message handler [{}]", handler.getName());
            singleLogoutServiceMessageHandlers.add(handler);
            AnnotationAwareOrderComparator.sort(this.singleLogoutServiceMessageHandlers);
        }
    }

    @Override
    public void registerLogoutRedirectionStrategy(final LogoutRedirectionStrategy strategy) {
        if (BeanSupplier.isNotProxy(strategy)) {
            LOGGER.trace("Registering logout redirection strategy [{}]", strategy.getName());
            logoutRedirectionStrategies.add(strategy);
            AnnotationAwareOrderComparator.sort(this.logoutRedirectionStrategies);
        }
    }
}
