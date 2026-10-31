package org.apereo.cas.pac4j;

import module java.base;
import org.apereo.cas.web.CasSessionStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;

/**
 * This is {@link CasSessionStorePac4jAdapter}. It lets pac4j, which still drives delegated authentication and the
 * OAuth and OpenID Connect protocol endpoints, keep its state in a {@link CasSessionStore}, so every module stores
 * session state with the same implementations whether or not it uses pac4j.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Getter
public class CasSessionStorePac4jAdapter implements SessionStore {
    private final CasSessionStore casSessionStore;

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        return casSessionStore.getSessionId(toJEEContext(context).getNativeRequest(), createSession);
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        return casSessionStore.get(toJEEContext(context).getNativeRequest(), key);
    }

    @Override
    public void set(final WebContext context, final String key, final @Nullable Object value) {
        val jeeContext = toJEEContext(context);
        casSessionStore.set(jeeContext.getNativeRequest(), jeeContext.getNativeResponse(), key, value);
    }

    @Override
    public boolean destroySession(final WebContext context) {
        val jeeContext = toJEEContext(context);
        return casSessionStore.destroySession(jeeContext.getNativeRequest(), jeeContext.getNativeResponse());
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        return casSessionStore.getTrackableSession(toJEEContext(context).getNativeRequest());
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        return casSessionStore.buildFromTrackableSession(toJEEContext(context).getNativeRequest(), trackableSession)
            .map(CasSessionStorePac4jAdapter::new);
    }

    @Override
    public boolean renewSession(final WebContext context) {
        return casSessionStore.renewSession(toJEEContext(context).getNativeRequest());
    }

    private static JEEContext toJEEContext(final WebContext context) {
        if (context instanceof final JEEContext jeeContext) {
            return jeeContext;
        }
        throw new IllegalArgumentException("Session state can only be kept for servlet requests, not for " + context.getClass().getName());
    }
}
