package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.web.CasSessionStore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * This is {@link DefaultCasSessionStore}. It keeps values in the container's http session,
 * under the key prefixed with the configured prefix. A store rebuilt from a trackable session
 * operates on that session rather than on the session of the current request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCasSessionStore implements CasSessionStore {
    private final String prefix;

    private final @Nullable HttpSession trackedSession;

    public DefaultCasSessionStore() {
        this(StringUtils.EMPTY);
    }

    public DefaultCasSessionStore(final String prefix) {
        this(prefix, null);
    }

    @Override
    public Optional<String> getSessionId(final HttpServletRequest request, final boolean create) {
        return getHttpSession(request, create).map(HttpSession::getId);
    }

    @Override
    public Optional<Object> get(final HttpServletRequest request, final String key) {
        return getHttpSession(request, false).map(session -> session.getAttribute(prefix + key));
    }

    @Override
    public void set(final HttpServletRequest request, final HttpServletResponse response,
                    final String key, final @Nullable Object value) {
        setSessionAttribute(request, key, value);
    }

    @Override
    public boolean destroySession(final HttpServletRequest request, final HttpServletResponse response) {
        getHttpSession(request, false).ifPresent(session -> {
            LOGGER.debug("Invalidating http session [{}]", session.getId());
            session.invalidate();
        });
        return true;
    }

    @Override
    public boolean renewSession(final HttpServletRequest request) {
        val attributes = new LinkedHashMap<String, Object>();
        val session = request.getSession(false);
        if (session != null) {
            LOGGER.debug("Discarding http session [{}] to renew it", session.getId());
            Collections.list(session.getAttributeNames()).forEach(name -> attributes.put(name, session.getAttribute(name)));
            session.invalidate();
        }
        val newSession = request.getSession(true);
        attributes.forEach(newSession::setAttribute);
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(final HttpServletRequest request) {
        return getHttpSession(request, false).map(Object.class::cast);
    }

    @Override
    public Optional<CasSessionStore> buildFromTrackableSession(final HttpServletRequest request, final Object trackableSession) {
        return trackableSession instanceof final HttpSession session
            ? Optional.of(new DefaultCasSessionStore(prefix, session))
            : Optional.empty();
    }

    /**
     * Get the http session this store operates on: the tracked session it was rebuilt from, if any,
     * or else the session of the request.
     *
     * @param request the request
     * @param create  whether the request session should be created when there is none
     * @return the http session
     */
    protected Optional<HttpSession> getHttpSession(final HttpServletRequest request, final boolean create) {
        return Optional.ofNullable(trackedSession).or(() -> Optional.ofNullable(request.getSession(create)));
    }

    /**
     * Store a value in the http session, creating the session when the value is not {@code null},
     * and removing the key otherwise. A session that was already invalidated is left alone.
     *
     * @param request the request
     * @param key     the key, without the prefix
     * @param value   the value
     */
    protected void setSessionAttribute(final HttpServletRequest request, final String key, final @Nullable Object value) {
        val prefixedKey = prefix + key;
        try {
            if (value == null) {
                getHttpSession(request, false).ifPresent(session -> session.removeAttribute(prefixedKey));
            } else {
                getHttpSession(request, true).orElseThrow().setAttribute(prefixedKey, value);
            }
        } catch (final IllegalStateException e) {
            LOGGER.debug("Unable to update [{}] since the http session is invalidated: [{}]", prefixedKey, e.getMessage());
        }
    }
}
