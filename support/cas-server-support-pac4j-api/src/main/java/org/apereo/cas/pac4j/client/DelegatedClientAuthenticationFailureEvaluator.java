package org.apereo.cas.pac4j.client;

import module java.base;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DelegatedClientAuthenticationFailureEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface DelegatedClientAuthenticationFailureEvaluator {
    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "delegatedClientAuthenticationFailureEvaluator";

    /**
     * Evaluate failure.
     *
     * @param request the request
     * @param status  the status
     * @return the optional
     */
    Optional<ModelAndView> evaluate(HttpServletRequest request, int status);
}
