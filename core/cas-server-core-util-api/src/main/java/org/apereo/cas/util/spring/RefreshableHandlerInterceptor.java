package org.apereo.cas.util.spring;

import module java.base;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link RefreshableHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class RefreshableHandlerInterceptor implements HandlerInterceptor {
    private ObjectProvider<? extends @NonNull HandlerInterceptor> delegate;

    private Supplier<List<? extends HandlerInterceptor>> delegateSupplier;

    public RefreshableHandlerInterceptor(final ObjectProvider<? extends @NonNull HandlerInterceptor> delegate) {
        this.delegate = delegate;
    }

    public RefreshableHandlerInterceptor(final Supplier<List<? extends HandlerInterceptor>> delegate) {
        this.delegateSupplier = delegate;
    }

    @Override
    public boolean preHandle(
        @NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler) {
        return getHandlerInterceptors()
            .stream()
            .allMatch(Unchecked.predicate(i -> i.preHandle(request, response, handler)));
    }

    @Override
    public void postHandle(
        @NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler, final ModelAndView modelAndView) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.postHandle(request, response, handler, modelAndView)));
    }

    @Override
    public void afterCompletion(
        @NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler, final Exception ex) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.afterCompletion(request, response, handler, ex)));
    }

    private List<? extends HandlerInterceptor> getHandlerInterceptors() {
        if (this.delegate != null) {
            return Optional.ofNullable(delegate.getIfAvailable()).map(List::of).orElseGet(List::of);
        }
        return this.delegateSupplier.get();
    }
}
