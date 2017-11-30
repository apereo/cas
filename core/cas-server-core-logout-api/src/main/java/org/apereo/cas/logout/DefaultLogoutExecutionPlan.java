package org.apereo.cas.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultLogoutExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultLogoutExecutionPlan implements LogoutExecutionPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogoutExecutionPlan.class);

    private final List<LogoutHandler> handlers = new ArrayList<>();

    @Override
    public void registerLogoutHandler(final LogoutHandler handler) {
        LOGGER.debug("Registering logout handler [{}]", handler.getName());
        handlers.add(handler);
    }

    @Override
    public Collection<LogoutHandler> getLogoutHandlers() {
        OrderComparator.sort(this.handlers);
        return this.handlers;
    }
}
