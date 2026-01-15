package org.apereo.cas.web;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;

/**
 * This is {@link CasWebSecurityConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasWebSecurityConfigurer<T> extends Ordered, DisposableBean {
    /**
     * Endpoint url used for admin-level form-login of endpoints.
     */
    String ENDPOINT_URL_ADMIN_FORM_LOGIN = "/adminlogin";
    
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
    default CasWebSecurityConfigurer<T> configure(final T object) throws Exception {
        return this;
    }

    /**
     * Finish.
     *
     * @param http the http
     */
    @CanIgnoreReturnValue
    default CasWebSecurityConfigurer<T> finish(final T http) throws Exception {
        return this;
    }

    @Override
    default void destroy() {}
}
