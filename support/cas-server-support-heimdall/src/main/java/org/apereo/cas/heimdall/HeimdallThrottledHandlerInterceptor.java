package org.apereo.cas.heimdall;

import module java.base;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Applies authentication throttling to the Heimdall endpoints while counting only failed caller
 * authentication ({@code 401}) as a failure. The throttling interceptors record every response
 * other than a success, and the default throttling filter claims every {@code POST}, so policy
 * denials ({@code 403}), unknown resources ({@code 404}) and malformed requests ({@code 400})
 * would otherwise lock out a legitimate caller. Throttled callers are still refused up front.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class HeimdallThrottledHandlerInterceptor implements HandlerInterceptor {
    private final HandlerInterceptor delegate;

    @Override
    public boolean preHandle(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
                             final @NonNull Object handler) throws Exception {
        return delegate.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
                           final @NonNull Object handler, final @Nullable ModelAndView modelAndView) throws Exception {
        if (isFailedAuthentication(response)) {
            delegate.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
                                final @NonNull Object handler, final @Nullable Exception ex) throws Exception {
        if (isFailedAuthentication(response)) {
            delegate.afterCompletion(request, response, handler, ex);
        }
    }

    private static boolean isFailedAuthentication(final HttpServletResponse response) {
        return response.getStatus() == HttpStatus.UNAUTHORIZED.value();
    }
}
