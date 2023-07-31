package org.apereo.cas.web;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
    @Override
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
     * @throws Exception the exception
     */
    @CanIgnoreReturnValue
    default ProtocolEndpointWebSecurityConfigurer<T> configure(final T object) throws Exception {
        return this;
    }

    /**
     * Finish.
     *
     * @param http the http
     */
    @CanIgnoreReturnValue
    default ProtocolEndpointWebSecurityConfigurer<T> finish(final T http) throws Exception {
        return this;
    }

}
