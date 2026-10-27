package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.web.CasSessionStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultCasSessionStore}. It keeps values in the container's http session,
 * under the key prefixed with the configured prefix.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultCasSessionStore implements CasSessionStore {
    private final String prefix;

    public DefaultCasSessionStore() {
        this(StringUtils.EMPTY);
    }

    @Override
    public Optional<Object> get(final HttpServletRequest request, final String key) {
        val session = request.getSession(false);
        if (session == null) {
            LOGGER.trace("No http session is available to get [{}]", key);
            return Optional.empty();
        }
        return Optional.ofNullable(session.getAttribute(prefix + key));
    }

    @Override
    public void set(final HttpServletRequest request, final HttpServletResponse response,
                    final String key, final @Nullable Object value) {
        setSessionAttribute(request, key, value);
    }

    @Override
    public Optional<Object> getTrackableSession(final HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false));
    }

    @Override
    public void buildFromTrackableSession(final HttpServletRequest request, final Object trackableSession) {
        LOGGER.trace("The http session is tracked by the servlet container; nothing to restore");
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
        if (value == null) {
            val session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(prefixedKey);
            }
        } else {
            try {
                request.getSession(true).setAttribute(prefixedKey, value);
            } catch (final IllegalStateException e) {
                LOGGER.debug("Unable to set [{}] since the http session is invalidated: [{}]", prefixedKey, e.getMessage());
            }
        }
    }
}
