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
     * The endpoints whose state-changing requests must carry a CSRF token, as path patterns. CSRF protection is configured
     * once for all configurers: a request that is not a {@code GET}, {@code HEAD}, {@code TRACE} or {@code OPTIONS} request
     * and whose path matches one of these patterns must carry the token. Configurers declare their endpoints here rather than
     * configuring CSRF themselves, since each such configuration would replace the others.
     *
     * @return the path patterns
     */
    default List<String> getCsrfProtectedEndpoints() {
        return List.of();
    }

    /**
     * The endpoints whose state-changing requests must carry a CSRF token when they are made by a browser, as path
     * patterns: a request to one of them that is not a {@code GET}, {@code HEAD}, {@code TRACE} or {@code OPTIONS} request,
     * carries a session cookie and no {@code Authorization} header must carry the token. A request that authenticates
     * itself, such as with basic authentication or a bearer token, is a client of its own and is not asked for one, so
     * scripts and other clients of such endpoints keep working as they are.
     *
     * @return the path patterns
     */
    default List<String> getCsrfProtectedSessionEndpoints() {
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
