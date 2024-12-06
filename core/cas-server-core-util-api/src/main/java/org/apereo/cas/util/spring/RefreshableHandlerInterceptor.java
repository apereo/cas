package org.apereo.cas.util.spring;

import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This is {@link RefreshableHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class RefreshableHandlerInterceptor implements HandlerInterceptor {
    private ObjectProvider<? extends HandlerInterceptor> delegate;

    private Supplier<List<? extends HandlerInterceptor>> delegateSupplier;

    public RefreshableHandlerInterceptor(final ObjectProvider<? extends HandlerInterceptor> delegate) {
        this.delegate = delegate;
    }

    public RefreshableHandlerInterceptor(final Supplier<List<? extends HandlerInterceptor>> delegate) {
        this.delegateSupplier = delegate;
    }

    @Override
    public boolean preHandle(
        @Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response,
        @Nonnull final Object handler) {
        return getHandlerInterceptors()
            .stream()
            .allMatch(Unchecked.predicate(i -> i.preHandle(request, response, handler)));
    }

    @Override
    public void postHandle(
        @Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response,
        @Nonnull final Object handler, final ModelAndView modelAndView) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.postHandle(request, response, handler, modelAndView)));
    }

    @Override
    public void afterCompletion(
        @Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response,
        @Nonnull final Object handler, final Exception ex) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.afterCompletion(request, response, handler, ex)));
    }

    private List<? extends HandlerInterceptor> getHandlerInterceptors() {
        if (this.delegate != null) {
            return Optional.ofNullable(delegate.getIfAvailable()).map(List::of).orElseGet(List::of);
        }
        return this.delegateSupplier.get();
    }
}
