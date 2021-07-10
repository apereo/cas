package org.apereo.cas.web;

import org.springframework.core.Ordered;

import java.util.List;

/**
 * This is {@link ProtocolEndpointConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface ProtocolEndpointConfigurer<T> extends Ordered {
    /**
     * Gets base endpoint.
     *
     * @return the base endpoint
     */
    List<String> getBaseEndpoints();

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Configure.
     *
     * @param value the value
     */
    default void configure(T value) {
    }
}
