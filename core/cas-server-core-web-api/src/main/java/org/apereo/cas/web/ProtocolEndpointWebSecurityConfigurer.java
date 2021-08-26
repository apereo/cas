package org.apereo.cas.web;

import org.springframework.core.Ordered;

import java.util.List;

/**
 * This is {@link ProtocolEndpointWebSecurityConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface ProtocolEndpointWebSecurityConfigurer<T> extends Ordered {

    /**
     * Get order of this configurer.
     *
     * @return the order.
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Gets base endpoint.
     *
     * @return the base endpoint
     */
    default List<String> getIgnoredEndpoints() {
        return List.of();
    }

    /**
     * Configure.
     *
     * @param object the object
     * @return the protocol endpoint configurer
     */
    default ProtocolEndpointWebSecurityConfigurer<T> configure(T object) {
        return this;
    }
}
