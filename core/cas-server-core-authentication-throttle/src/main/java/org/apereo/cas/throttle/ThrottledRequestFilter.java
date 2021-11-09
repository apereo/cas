package org.apereo.cas.throttle;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledRequestFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface ThrottledRequestFilter extends Ordered {
    /**
     * Filer that only supports POST requests.
     *
     * @return the throttled request filter
     */
    static ThrottledRequestFilter httpPost() {
        return (request, response) -> HttpMethod.POST.name().equals(request.getMethod());
    }

    /**
     * Determine if this request should be supported.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    boolean supports(HttpServletRequest request, HttpServletResponse response);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
