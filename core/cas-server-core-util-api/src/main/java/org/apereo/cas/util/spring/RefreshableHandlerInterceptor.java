package org.apereo.cas.util.spring;

import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is {@link RefreshableHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class RefreshableHandlerInterceptor implements HandlerInterceptor {
    private ObjectProvider<? extends HandlerInterceptor> delegate;

    private Supplier<List<HandlerInterceptor>> delegateSupplier;

    public RefreshableHandlerInterceptor(final ObjectProvider<? extends HandlerInterceptor> delegate) {
        this.delegate = delegate;
    }

    public RefreshableHandlerInterceptor(final Supplier<List<HandlerInterceptor>> delegate) {
        this.delegateSupplier = delegate;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        return getHandlerInterceptors()
            .stream()
            .allMatch(Unchecked.predicate(i -> i.preHandle(request, response, handler)));
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                           final Object handler, final ModelAndView modelAndView) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.postHandle(request, response, handler, modelAndView)));
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        getHandlerInterceptors().forEach(Unchecked.consumer(i -> i.afterCompletion(request, response, handler, ex)));
    }

    private List<HandlerInterceptor> getHandlerInterceptors() {
        if (this.delegate != null) {
            return List.of(delegate.getObject());
        }
        return this.delegateSupplier.get();
    }
}
