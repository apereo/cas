package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultLogoutExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class DefaultLogoutExecutionPlan implements LogoutExecutionPlan {
    private final List<LogoutRedirectionStrategy> logoutRedirectionStrategies = new ArrayList<>(0);
    
    private final List<LogoutPostProcessor> logoutPostProcessors = new ArrayList<>(0);

    private final List<SingleLogoutServiceMessageHandler> singleLogoutServiceMessageHandlers = new ArrayList<>(0);

    @Override
    public void registerLogoutPostProcessor(final LogoutPostProcessor handler) {
        LOGGER.debug("Registering logout handler [{}]", handler.getName());
        logoutPostProcessors.add(handler);
        AnnotationAwareOrderComparator.sort(this.logoutPostProcessors);
    }

    @Override
    public void registerSingleLogoutServiceMessageHandler(final SingleLogoutServiceMessageHandler handler) {
        LOGGER.trace("Registering single logout service message handler [{}]", handler.getName());
        singleLogoutServiceMessageHandlers.add(handler);
        AnnotationAwareOrderComparator.sort(this.singleLogoutServiceMessageHandlers);
    }

    @Override
    public void registerLogoutRedirectionStrategy(final LogoutRedirectionStrategy strategy) {
        LOGGER.trace("Registering logout redirection strategy [{}]", strategy.getName());
        logoutRedirectionStrategies.add(strategy);
        AnnotationAwareOrderComparator.sort(this.logoutRedirectionStrategies);
    }
}
