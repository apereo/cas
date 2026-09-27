package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Throttles caller authentication on the Heimdall endpoints, which covers guessing of CAS user
 * passwords on {@code /heimdall/authorize} and of client secrets on {@code /heimdall/authzen}.
 * The throttling interceptor records any response other than a success as a failure, so this filter
 * only claims a request while its status is still a success or once it has become a {@code 401}:
 * policy denials, unknown resources and malformed requests are not failed logins.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class HeimdallThrottledRequestFilter implements ThrottledRequestFilter {
    /**
     * The throttled endpoint paths.
     */
    public static final List<String> ENDPOINTS = List.of(
        HeimdallAuthorizationController.BASE_URL + "/authorize",
        HeimdallAuthorizationController.BASE_URL + "/authzen");

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val status = response.getStatus();
        return HttpMethod.POST.matches(request.getMethod())
            && ENDPOINTS.stream().anyMatch(request.getRequestURI()::endsWith)
            && (status == HttpStatus.OK.value() || status == HttpStatus.UNAUTHORIZED.value());
    }
}
