package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Disposes authentication handlers built for a single use, such as the handlers of a tenant, which hold
 * connection pools and other resources. Within an HTTP request, a handler is disposed once, when the request
 * completes, however many times it was handed out or released during the request. Outside a request,
 * releasing a handler disposes it right away.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@UtilityClass
public class DisposableAuthenticationHandlers {
    private static final String REQUEST_ATTRIBUTE = DisposableAuthenticationHandlers.class.getName();

    /**
     * Track handlers built for the current request, so they are disposed when the request completes.
     * Outside a request, the caller remains responsible for them.
     *
     * @param handlers the handlers
     */
    public static void track(final Collection<? extends AuthenticationHandler> handlers) {
        deferToRequestCompletion(handlers);
    }

    /**
     * Release handlers that are no longer needed: they are disposed when the current request completes,
     * or right away outside a request.
     *
     * @param handlers the handlers
     */
    public static void release(final Collection<? extends AuthenticationHandler> handlers) {
        if (!deferToRequestCompletion(handlers)) {
            disposableHandlers(handlers).forEach(DisposableAuthenticationHandlers::dispose);
        }
    }

    private static boolean deferToRequestCompletion(final Collection<? extends AuthenticationHandler> handlers) {
        val attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        val disposables = disposableHandlers(handlers);
        if (!disposables.isEmpty()) {
            pendingHandlers(attributes).add(disposables);
        }
        return true;
    }

    private static PendingHandlers pendingHandlers(final RequestAttributes attributes) {
        if (attributes.getAttribute(REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) instanceof final PendingHandlers existing
            && !existing.isDisposed()) {
            return existing;
        }
        val pending = new PendingHandlers();
        attributes.setAttribute(REQUEST_ATTRIBUTE, pending, RequestAttributes.SCOPE_REQUEST);
        attributes.registerDestructionCallback(REQUEST_ATTRIBUTE, pending, RequestAttributes.SCOPE_REQUEST);
        return pending;
    }

    private static List<DisposableBean> disposableHandlers(final Collection<? extends AuthenticationHandler> handlers) {
        return handlers
            .stream()
            .filter(AuthenticationHandler::isDisposable)
            .filter(DisposableBean.class::isInstance)
            .map(DisposableBean.class::cast)
            .toList();
    }

    private static void dispose(final DisposableBean handler) {
        try {
            handler.destroy();
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    private static final class PendingHandlers implements Runnable {
        private final Set<DisposableBean> handlers = Collections.newSetFromMap(new IdentityHashMap<>());

        private boolean disposed;

        void add(final Collection<DisposableBean> disposables) {
            handlers.addAll(disposables);
        }

        boolean isDisposed() {
            return disposed;
        }

        @Override
        public void run() {
            disposed = true;
            handlers.forEach(DisposableAuthenticationHandlers::dispose);
            handlers.clear();
        }
    }
}
