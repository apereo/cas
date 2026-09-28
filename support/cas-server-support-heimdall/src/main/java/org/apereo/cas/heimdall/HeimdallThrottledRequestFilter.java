package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Subjects caller authentication on the Heimdall endpoints to authentication throttling, which covers guessing
 * of CAS user passwords on {@code /heimdall/authorize} and of client secrets on {@code /heimdall/authzen}.
 * Only failed caller authentication ({@code 401}) is recorded; denials and malformed requests are not.
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
        HeimdallAuthorizationController.BASE_URL + HeimdallAuthorizationController.AUTHZEN_PATH,
        HeimdallAuthorizationController.BASE_URL + HeimdallAuthorizationController.AUTHZEN_EVALUATIONS_PATH);

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        return HttpMethod.POST.matches(request.getMethod())
            && ENDPOINTS.stream().anyMatch(request.getRequestURI()::endsWith);
    }
}
